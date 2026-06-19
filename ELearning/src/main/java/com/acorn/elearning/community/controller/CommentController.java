package com.acorn.elearning.community.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CommentController {

    @PostMapping("/community/posts/{postId}/comments")
    public String create(@PathVariable Long postId) {
        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
        // if (bindingResult.hasErrors()) { return "/community/board"; }
        // SessionUser sessionUser = currentSessionUser();
        // commentService.create(sessionUser, form, postId);
        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
        return "redirect:/community/board";
    }
}
