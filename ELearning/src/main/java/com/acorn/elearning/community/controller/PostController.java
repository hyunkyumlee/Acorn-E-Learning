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
    public String list(@ModelAttribute PostSearchCondition condition, Model model) {
        model.addAttribute("screen", "community/board");
        model.addAttribute("condition", condition);
        model.addAttribute("view", postService.page(condition));
        return "community/board";
    }

    @GetMapping("/community/posts/{postId}")
    public String detail(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long postId,
            Model model
    ) {
        model.addAttribute("screen", "community/detail");
        model.addAttribute("view", postService.detail(sessionUser, postId));
        model.addAttribute("currentUserId", sessionUser == null ? null : sessionUser.userId());
        model.addAttribute("commentForm", new CommentForm());
        model.addAttribute("reportForm", new ReportForm());
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
}