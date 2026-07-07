package com.acorn.elearning.community.controller;

import com.acorn.elearning.community.dto.response.PostDetailResponse;
import com.acorn.elearning.community.dto.response.PostPageResponse;
import com.acorn.elearning.community.form.CommentForm;
import com.acorn.elearning.community.form.PostForm;
import com.acorn.elearning.community.form.PostSearchCondition;
import com.acorn.elearning.community.form.ReportForm;
import com.acorn.elearning.community.model.Comment;
import com.acorn.elearning.community.model.CommunityPost;
import com.acorn.elearning.community.service.PostService;
import com.acorn.elearning.security.SessionUser;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/community/board")
    public String list(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @ModelAttribute PostSearchCondition condition,
            Model model
    ) {
        PostSearchCondition hotCondition = new PostSearchCondition();
        hotCondition.setSort("hot");
        hotCondition.setSize(8);
        PostPageResponse view = postService.page(condition);
        var hotView = postService.page(hotCondition);
        Set<Long> hotPostIds = hotView.posts().stream()
                .map(CommunityPost::getPostId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        hotPostIds.addAll(subjectHotPostIds(view.posts()));
        view = blendHotPosts(view, hotPostIds, condition);

        model.addAttribute("screen", "community/board");
        model.addAttribute("condition", condition);
        model.addAttribute("view", view);
        model.addAttribute("hotView", hotView);
        model.addAttribute("hotPostIds", hotPostIds);
        model.addAttribute("writerNicknames", writerNicknames(view, hotView));
        addCommunityShell(model, sessionUser, condition);
        return "community/board";
    }

    @GetMapping("/community/posts/{postId}")
    public String detail(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long postId,
            Model model
    ) {
        PostDetailResponse view = postService.detail(sessionUser, postId);
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
            Model model
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("screen", "community/write");
        model.addAttribute("form", new PostForm());
        addCommunityShell(model, sessionUser, new PostSearchCondition());
        return "community/write";
    }

    @PostMapping("/community/posts")
    public String create(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @Valid @ModelAttribute("form") PostForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("screen", "community/write");
            addCommunityShell(model, sessionUser, conditionFromForm(form));
            return "community/write";
        }
        CommunityPost post = postService.create(sessionUser, form);
        redirectAttributes.addFlashAttribute("message", "게시글이 등록되었습니다.");
        return "redirect:/community/posts/" + post.getPostId();
    }

    @GetMapping("/community/posts/{postId}/edit")
    public String editForm(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long postId,
            Model model
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("screen", "community/edit");
        PostDetailResponse view = postService.detail(sessionUser, postId);
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
            model.addAttribute("view", postService.detail(sessionUser, postId));
            addCommunityShell(model, sessionUser, conditionFromForm(form));
            return "community/edit";
        }
        postService.update(sessionUser, postId, form);
        redirectAttributes.addFlashAttribute("message", "게시글이 수정되었습니다.");
        return "redirect:/community/posts/" + postId;
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
        Map<Long, String> nicknames = new LinkedHashMap<>();
        for (PostPageResponse view : views) {
            view.posts().forEach(post -> nicknames.putIfAbsent(post.getWriterId(), nicknameFor(post.getWriterId())));
        }
        return nicknames;
    }

    private PostPageResponse blendHotPosts(PostPageResponse view, Set<Long> hotPostIds, PostSearchCondition condition) {
        if (view == null || hotPostIds == null || condition == null || condition.hotSort()) {
            return view;
        }
        List<CommunityPost> hotPosts = new ArrayList<>();
        List<CommunityPost> normalPosts = new ArrayList<>();
        for (CommunityPost post : view.posts()) {
            if (hotPostIds.contains(post.getPostId())) {
                hotPosts.add(post);
            } else {
                normalPosts.add(post);
            }
        }
        if (hotPosts.size() <= 1 || normalPosts.isEmpty()) {
            return view;
        }

        List<CommunityPost> blended = new ArrayList<>(view.posts().size());
        int hotIndex = 0;
        int normalIndex = 0;
        int normalStreak = 0;

        blended.add(hotPosts.get(hotIndex++));
        blended.add(normalPosts.get(normalIndex++));
        if (hotIndex < hotPosts.size()) {
            blended.add(hotPosts.get(hotIndex++));
        }

        while (hotIndex < hotPosts.size() || normalIndex < normalPosts.size()) {
            if (normalIndex < normalPosts.size() && (normalStreak < 2 || hotIndex >= hotPosts.size())) {
                blended.add(normalPosts.get(normalIndex++));
                normalStreak++;
                continue;
            }
            if (hotIndex < hotPosts.size()) {
                blended.add(hotPosts.get(hotIndex++));
                normalStreak = 0;
                continue;
            }
            blended.add(normalPosts.get(normalIndex++));
        }

        return new PostPageResponse(
                blended,
                view.total(),
                view.page(),
                view.size(),
                view.totalPages(),
                view.sort()
        );
    }

    private Set<Long> subjectHotPostIds(List<CommunityPost> posts) {
        Map<Long, List<CommunityPost>> postsBySubject = new LinkedHashMap<>();
        for (CommunityPost post : posts) {
            postsBySubject.computeIfAbsent(post.getSubjectId(), key -> new ArrayList<>()).add(post);
        }

        Set<Long> ids = new LinkedHashSet<>();
        for (List<CommunityPost> subjectPosts : postsBySubject.values()) {
            subjectPosts.stream()
                    .sorted(Comparator
                            .comparingInt(this::hotScore)
                            .thenComparing(CommunityPost::getPostId, Comparator.nullsLast(Comparator.naturalOrder()))
                            .reversed())
                    .limit(2)
                    .map(CommunityPost::getPostId)
                    .forEach(ids::add);
        }
        return ids;
    }

    private int hotScore(CommunityPost post) {
        return safeCount(post.getLikeCount()) * 3
                + safeCount(post.getCommentCount()) * 2
                + safeCount(post.getScrapCount());
    }

    private int safeCount(Integer count) {
        return count == null ? 0 : count;
    }

    private Map<Long, String> writerNicknames(PostDetailResponse view) {
        Map<Long, String> nicknames = new LinkedHashMap<>();
        nicknames.put(view.post().getWriterId(), nicknameFor(view.post().getWriterId()));
        view.comments().stream()
                .map(Comment::getWriterId)
                .forEach(writerId -> nicknames.putIfAbsent(writerId, nicknameFor(writerId)));
        return nicknames;
    }

    private String nicknameFor(Long writerId) {
        if (writerId == null) {
            return "커뮤니티러";
        }
        String[] names = {
                "코드하루",
                "자바노트",
                "쿼리연습생",
                "파이썬친구",
                "웹기록장",
                "디버깅중",
                "스터디물결",
                "개념정리왕",
                "스프링새싹",
                "알고한걸음"
        };
        return names[(int) Math.floorMod(writerId - 1, names.length)];
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
