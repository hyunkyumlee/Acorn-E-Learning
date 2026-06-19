package com.acorn.elearning.community.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ReactionController {

    @PostMapping("/community/posts/{postId}/likes")
    public String like(@PathVariable Long postId) {
        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
        // if (bindingResult.hasErrors()) { return "/community/board"; }
        // SessionUser sessionUser = currentSessionUser();
        // reactionService.like(sessionUser, form, postId);
        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
        return "redirect:/community/board";
    }

    @PostMapping("/community/posts/{postId}/scraps")
    public String scrap(@PathVariable Long postId) {
        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
        // if (bindingResult.hasErrors()) { return "/community/board"; }
        // SessionUser sessionUser = currentSessionUser();
        // reactionService.scrap(sessionUser, form, postId);
        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
        return "redirect:/community/board";
    }
}
