package com.acorn.elearning.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AdminController {

    @GetMapping("/admin")
    public String dashboard(Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // AdminDashboardView view = adminService.dashboard(sessionUser);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
        model.addAttribute("screen", "admin/dashboard");
        return "admin/dashboard";
    }

    @GetMapping("/admin/login")
    public String loginForm(Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // AuthPageView view = adminService.loginForm(sessionUser);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
        model.addAttribute("screen", "admin/login");
        return "admin/login";
    }

    @PostMapping("/admin/login")
    public String login() {
        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
        // if (bindingResult.hasErrors()) { return "admin/dashboard"; }
        // SessionUser sessionUser = currentSessionUser();
        // authService.login(sessionUser, form);
        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
        return "redirect:/admin";
    }
}
