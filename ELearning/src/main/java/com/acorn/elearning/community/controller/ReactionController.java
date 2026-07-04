package com.acorn.elearning.community.controller;

import com.acorn.elearning.community.service.ReactionService;
import com.acorn.elearning.security.SessionUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ReactionController {
    private final ReactionService reactionService;

    public ReactionController(ReactionService reactionService) {
        this.reactionService = reactionService;
    }

    @PostMapping("/community/posts/{postId}/likes")
    public String like(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long postId,
            RedirectAttributes redirectAttributes
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        reactionService.like(sessionUser, postId);
        redirectAttributes.addFlashAttribute("message", "좋아요를 눌렀습니다.");
        return "redirect:/community/posts/" + postId;
    }

    @PostMapping("/community/posts/{postId}/likes/delete")
    public String unlike(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long postId,
            RedirectAttributes redirectAttributes
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        reactionService.unlike(sessionUser, postId);
        redirectAttributes.addFlashAttribute("message", "좋아요를 취소했습니다.");
        return "redirect:/community/posts/" + postId;
    }

    @PostMapping("/community/posts/{postId}/scraps")
    public String scrap(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long postId,
            RedirectAttributes redirectAttributes
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        reactionService.scrap(sessionUser, postId);
        redirectAttributes.addFlashAttribute("message", "스크랩했습니다.");
        return "redirect:/community/posts/" + postId;
    }

    @PostMapping("/community/posts/{postId}/scraps/delete")
    public String unscrap(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long postId,
            RedirectAttributes redirectAttributes
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        reactionService.unscrap(sessionUser, postId);
        redirectAttributes.addFlashAttribute("message", "스크랩을 취소했습니다.");
        return "redirect:/community/posts/" + postId;
    }
}