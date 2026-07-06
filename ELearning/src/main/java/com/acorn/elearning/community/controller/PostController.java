package com.acorn.elearning.community.controller;

import com.acorn.elearning.community.dto.response.PostDetailResponse;
import com.acorn.elearning.community.form.CommentForm;
import com.acorn.elearning.community.form.PostForm;
import com.acorn.elearning.community.form.PostSearchCondition;
import com.acorn.elearning.community.form.ReportForm;
import com.acorn.elearning.community.model.CommunityPost;
import com.acorn.elearning.community.service.PostService;
import com.acorn.elearning.security.SessionUser;
import jakarta.validation.Valid;
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
        hotCondition.setSize(5);

        model.addAttribute("screen", "community/board");
        model.addAttribute("condition", condition);
        model.addAttribute("view", postService.page(condition));
        model.addAttribute("hotView", postService.page(hotCondition));
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
        PostSearchCondition condition = new PostSearchCondition();
        condition.setSubjectId(view.post().getSubjectId());
        condition.setBoardType(view.post().getBoardType());

        model.addAttribute("screen", "community/detail");
        model.addAttribute("view", view);
        model.addAttribute("currentUserId", sessionUser == null ? null : sessionUser.userId());
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
}