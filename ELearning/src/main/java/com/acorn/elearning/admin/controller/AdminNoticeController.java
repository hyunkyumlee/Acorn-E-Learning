package com.acorn.elearning.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AdminNoticeController {

    @GetMapping("/admin/notices")
    public String notices(Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // AdminManagePageView view = adminNoticeService.notices(sessionUser);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
        model.addAttribute("screen", "admin/notices");
        return "admin/notices";
    }

    @PostMapping("/admin/notices")
    public String create() {
        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
        // if (bindingResult.hasErrors()) { return "admin/notices"; }
        // SessionUser sessionUser = currentSessionUser();
        // adminNoticeService.create(sessionUser, form);
        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
        return "redirect:/admin/notices";
    }

    @PostMapping("/admin/notices/{noticeId}")
    public String update(@PathVariable Long noticeId) {
        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
        // if (bindingResult.hasErrors()) { return "/admin/notices"; }
        // SessionUser sessionUser = currentSessionUser();
        // adminNoticeService.update(sessionUser, form, noticeId);
        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
        return "redirect:/admin/notices";
    }
}
