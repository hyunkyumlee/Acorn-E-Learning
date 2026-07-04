package com.acorn.elearning.community.controller;

import com.acorn.elearning.community.form.CommentForm;
import com.acorn.elearning.community.service.CommentService;
import com.acorn.elearning.security.SessionUser;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/community/posts/{postId}/comments")
    public String create(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long postId,
            @Valid @ModelAttribute("commentForm") CommentForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        if (!bindingResult.hasErrors()) {
            commentService.create(sessionUser, postId, form);
            redirectAttributes.addFlashAttribute("message", "댓글이 등록되었습니다.");
        }
        return "redirect:/community/posts/" + postId;
    }

    @PostMapping("/community/comments/{commentId}/update")
    public String update(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long commentId,
            @RequestParam Long postId,
            @Valid @ModelAttribute("commentForm") CommentForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        if (!bindingResult.hasErrors()) {
            commentService.update(sessionUser, commentId, form);
            redirectAttributes.addFlashAttribute("message", "댓글이 수정되었습니다.");
        }
        return "redirect:/community/posts/" + postId;
    }

    @PostMapping("/community/comments/{commentId}/delete")
    public String delete(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long commentId,
            @RequestParam Long postId,
            RedirectAttributes redirectAttributes
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        commentService.delete(sessionUser, commentId);
        redirectAttributes.addFlashAttribute("message", "댓글이 삭제되었습니다.");
        return "redirect:/community/posts/" + postId;
    }
}