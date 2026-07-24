package com.acorn.elearning.community.controller;

import com.acorn.elearning.common.idempotency.IdempotencyToken;
import com.acorn.elearning.common.idempotency.IdempotencyTokenService;
import com.acorn.elearning.community.dto.response.PostDetailResponse;
import com.acorn.elearning.community.dto.response.PostPageResponse;
import com.acorn.elearning.community.form.CommentForm;
import com.acorn.elearning.community.form.PostForm;
import com.acorn.elearning.community.form.PostSearchCondition;
import com.acorn.elearning.community.form.ReportForm;
import com.acorn.elearning.community.mapper.CommunityNoticeMapper;
import com.acorn.elearning.community.mapper.CommunityWriterMapper;
import com.acorn.elearning.community.model.Comment;
import com.acorn.elearning.community.model.CommunityPost;
import com.acorn.elearning.community.service.PostService;
import com.acorn.elearning.practice.view.WrongAnswerNote;
import com.acorn.elearning.security.SessionUser;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PostController {
    private static final int BOARD_NOTICE_LIMIT = 4;

    private final PostService postService;
    private final CommunityWriterMapper communityWriterMapper;
    private final CommunityNoticeMapper communityNoticeMapper;

    private static final String CREATE_POST_FORM_TYPE = "COMMUNITY_POST_CREATE";

    private final IdempotencyTokenService idempotencyTokenService;

    public PostController(
            PostService postService,
            CommunityWriterMapper communityWriterMapper,
            CommunityNoticeMapper communityNoticeMapper,
            IdempotencyTokenService idempotencyTokenService
    ) {
        this.postService = postService;
        this.communityWriterMapper = communityWriterMapper;
        this.communityNoticeMapper = communityNoticeMapper;
        this.idempotencyTokenService = idempotencyTokenService;
    }

    @GetMapping("/community/board")
    public String list(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @ModelAttribute PostSearchCondition condition,
            Model model
    ) {
        condition.setSize(20);
        PostPageResponse view = postService.page(condition);

        model.addAttribute("screen", "community/board");
        model.addAttribute("condition", condition);
        model.addAttribute("view", view);
        model.addAttribute("pageStart", Math.max(1, view.page() - 2));
        model.addAttribute("pageEnd", Math.min(view.totalPages(), view.page() + 2));
        model.addAttribute("writerNicknames", writerNicknames(view));
        model.addAttribute("notices", communityNoticeMapper.findPublishedNotices(BOARD_NOTICE_LIMIT));
        addCommunityShell(model, sessionUser, condition);
        return "community/board";
    }

    @GetMapping("/community/posts/{postId}")
    public String detail(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long postId,
            @RequestParam(name = "skipView", defaultValue = "false") boolean skipView,
            Model model
    ) {
        PostDetailResponse view = postService.detail(sessionUser, postId, !skipView);
        view.post().setContent(removeSampleMarker(view.post().getContent()));
        PostSearchCondition condition = new PostSearchCondition();
        condition.setSubjectId(view.post().getSubjectId());
        condition.setBoardType(view.post().getBoardType());

        model.addAttribute("screen", "community/detail");
        model.addAttribute("view", view);
        model.addAttribute("currentUserId", sessionUser == null ? null : sessionUser.userId());
        model.addAttribute("writerNicknames", writerNicknames(view));
        model.addAttribute("commentForm", new CommentForm());
        model.addAttribute("reportForm", new ReportForm());
        addCommunityShell(model, sessionUser, condition);
        return "community/detail";
    }

    @GetMapping("/community/write")
    public String createForm(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            Model model,
            HttpSession httpSession
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("screen", "community/write");
        PostForm form = new PostForm();
        Object wrongAnswerCommunityDraft = model.asMap().get("wrongAnswerCommunityDraft");
        if (wrongAnswerCommunityDraft instanceof WrongAnswerNote note) {
            form.setTitle(note.postTitle());
            form.setContent(note.markdown());
            form.setSubjectId(note.subjectId());
            form.setBoardType("STUDY_LOG");
            model.addAttribute("communityDraftNotice", "오답노트 내용을 불러왔습니다. 수정한 뒤 등록해 주세요.");
        }
        form.setIdempotencyToken(
                idempotencyTokenService.issue(
                        CREATE_POST_FORM_TYPE,
                        httpSession,
                        sessionUser.userId()
                ).token()
        );
        model.addAttribute("form", form);
        addCommunityShell(model, sessionUser, new PostSearchCondition());
        return "community/write";
    }

    @PostMapping("/community/posts")
    public String create(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @Valid @ModelAttribute("form") PostForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model,
            HttpSession httpSession
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("screen", "community/write");
            addCommunityShell(model, sessionUser, conditionFromForm(form));
            return "community/write";
        }

        idempotencyTokenService.requireAndConsume(
                form.getIdempotencyToken(),
                "",
                httpSession
        );

        CommunityPost post = form.getDraftPostId() == null
                ? postService.create(sessionUser, form)
                : postService.publishDraft(sessionUser, form);
        redirectAttributes.addFlashAttribute("message", "게시글이 등록되었습니다.");
        return "redirect:/community/posts/" + post.getPostId() + "?skipView=true";
    }

    @GetMapping("/community/posts/{postId}/edit")
    public String editForm(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long postId,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("screen", "community/edit");
        PostDetailResponse view = postService.detail(sessionUser, postId, false);
        if(!view.owner()){
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "본인이 작성한 게시글만 수정할 수 있습니다."
            );
            return "redirect:/community/posts/" + postId;
        }
        PostForm form = new PostForm();
        form.setSubjectId(view.post().getSubjectId());
        form.setBoardType(view.post().getBoardType());
        form.setTitle(view.post().getTitle());
        form.setContent(view.post().getContent());
        model.addAttribute("view", view);
        model.addAttribute("form", form);
        addCommunityShell(model, sessionUser, conditionFromForm(form));
        return "community/edit";
    }

    @PostMapping("/community/posts/{postId}/update")
    public String update(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long postId,
            @Valid @ModelAttribute("form") PostForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("screen", "community/edit");
            model.addAttribute("view", postService.detail(sessionUser, postId, false));
            addCommunityShell(model, sessionUser, conditionFromForm(form));
            return "community/edit";
        }
        postService.update(sessionUser, postId, form);
        redirectAttributes.addFlashAttribute("message", "게시글이 수정되었습니다.");
        return "redirect:/community/posts/" + postId + "?skipView=true";
    }

    @PostMapping("/community/posts/{postId}/delete")
    public String delete(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long postId,
            RedirectAttributes redirectAttributes
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        postService.delete(sessionUser, postId);
        redirectAttributes.addFlashAttribute("message", "게시글이 삭제되었습니다.");
        return "redirect:/community/board";
    }

    private void addCommunityShell(Model model, SessionUser sessionUser, PostSearchCondition condition) {
        model.addAttribute("activeSubjectId", condition.getSubjectId() == null ? "" : condition.getSubjectId().toString());
        model.addAttribute("activeBoardType", condition.getBoardType() == null ? "" : condition.getBoardType());
        model.addAttribute("loggedIn", sessionUser != null);
        model.addAttribute("profileName", sessionUser == null ? "guest" : sessionUser.nickname());
        model.addAttribute("profileImageUrl", sessionUser == null ? null : sessionUser.profileImageUrl());
        model.addAttribute("profileEmail", sessionUser == null ? "로그인하면 커뮤니티 활동을 확인할 수 있어." : sessionUser.email());
        if (sessionUser != null) {
            model.addAttribute("profileSummary", postService.profile(sessionUser));
        }
    }

    private PostSearchCondition conditionFromForm(PostForm form) {
        PostSearchCondition condition = new PostSearchCondition();
        condition.setSubjectId(form.getSubjectId());
        condition.setBoardType(form.getBoardType());
        return condition;
    }

    private Map<Long, String> writerNicknames(PostPageResponse... views) {
        Set<Long> writerIds = new HashSet<>();
        for (PostPageResponse view : views) {
            view.posts().forEach(post -> addWriterId(writerIds, post.getWriterId()));
        }
        return writerNicknames(writerIds);
    }

    private Map<Long, String> writerNicknames(PostDetailResponse view) {
        Set<Long> writerIds = new HashSet<>();
        addWriterId(writerIds, view.post().getWriterId());
        view.comments().stream()
                .map(Comment::getWriterId)
                .forEach(writerId -> addWriterId(writerIds, writerId));
        return writerNicknames(writerIds);
    }

    private Map<Long, String> writerNicknames(Collection<Long> writerIds) {
        Map<Long, String> nicknames = new LinkedHashMap<>();
        if (writerIds == null || writerIds.isEmpty()) {
            return nicknames;
        }
        communityWriterMapper.findNicknamesByUserIds(writerIds)
                .forEach(row -> nicknames.put(row.getWriterId(), row.getNickname()));
        writerIds.forEach(writerId -> nicknames.putIfAbsent(writerId, "사용자 " + writerId));
        return nicknames;
    }

    private void addWriterId(Set<Long> writerIds, Long writerId) {
        if (writerId != null) {
            writerIds.add(writerId);
        }
    }

    private String removeSampleMarker(String content) {
        if (content == null) {
            return "";
        }
        return content
                .replace("[sample-20260707-more-realistic-list]", "")
                .replace("sample-20260707-more-realistic-list", "")
                .trim();
    }
}
