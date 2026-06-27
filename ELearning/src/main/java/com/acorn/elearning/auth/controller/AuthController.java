package com.acorn.elearning.auth.controller;

import com.acorn.elearning.security.SessionUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginForm(Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // AuthPageView view = authService.loginForm(sessionUser);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
        model.addAttribute("screen", "auth/login");
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser) {
        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
        // if (bindingResult.hasErrors()) { return "auth/login"; }
        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
        if (sessionUser != null) {
            return "redirect:" + sessionUser.defaultRedirectPath();
        }
        return "redirect:/learning";
    }

    @GetMapping("/signup")
    public String signupForm(Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // AuthPageView view = authService.signupForm(sessionUser);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
        model.addAttribute("screen", "auth/signup");
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signup() {
        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
        // if (bindingResult.hasErrors()) { return "auth/signup"; }
        // SessionUser sessionUser = currentSessionUser();
        // authService.signup(sessionUser, form);
        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
        return "redirect:/learning/onboarding";
    }

    @PostMapping("/logout")
    public String logout() {
        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
        // if (bindingResult.hasErrors()) { return "/login"; }
        // SessionUser sessionUser = currentSessionUser();
        // sessionService.logout(sessionUser, form);
        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
        return "redirect:/login";
    }
}
