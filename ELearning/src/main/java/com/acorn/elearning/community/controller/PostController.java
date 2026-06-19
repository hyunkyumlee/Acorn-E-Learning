package com.acorn.elearning.community.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class PostController {

    @GetMapping("/community/board")
    public String list(Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // PostPageView view = postService.list(sessionUser);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
        model.addAttribute("screen", "community/board");
        return "community/board";
    }

    @GetMapping("/community/posts/{postId}")
    public String detail(@PathVariable Long postId, Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // PostDetailView view = postService.detail(sessionUser, postId);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
        model.addAttribute("screen", "community/detail");
        return "community/detail";
    }

    @GetMapping("/community/write")
    public String createForm(Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // PostPageView view = postService.createForm(sessionUser);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
        model.addAttribute("screen", "community/write");
        return "community/write";
    }

    @PostMapping("/community/posts")
    public String create() {
        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
        // if (bindingResult.hasErrors()) { return "community/board"; }
        // SessionUser sessionUser = currentSessionUser();
        // postService.create(sessionUser, form);
        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
        return "redirect:/community/board";
    }

    @GetMapping("/community/posts/{postId}/edit")
    public String editForm(@PathVariable Long postId, Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // PostDetailView view = postService.editForm(sessionUser, postId);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
        model.addAttribute("screen", "community/edit");
        return "community/edit";
    }

    @PostMapping("/community/posts/{postId}/update")
    public String update(@PathVariable Long postId) {
        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
        // if (bindingResult.hasErrors()) { return "community/edit"; }
        // SessionUser sessionUser = currentSessionUser();
        // postService.update(sessionUser, form, postId);
        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
        return "redirect:/community/board";
    }

    @PostMapping("/community/posts/{postId}/delete")
    public String delete(@PathVariable Long postId) {
        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
        // if (bindingResult.hasErrors()) { return "/community/board"; }
        // SessionUser sessionUser = currentSessionUser();
        // postService.delete(sessionUser, form, postId);
        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
        return "redirect:/community/board";
    }
}
