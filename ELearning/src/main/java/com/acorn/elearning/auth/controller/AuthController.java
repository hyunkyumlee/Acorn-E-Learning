package com.acorn.elearning.auth.controller;

import com.acorn.elearning.auth.form.LoginForm;
import com.acorn.elearning.auth.service.AuthService;
import com.acorn.elearning.auth.service.SessionService;
import com.acorn.elearning.security.SessionUser;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    private final AuthService authService;
    private final SessionService sessionService;

    public AuthController(AuthService authService, SessionService sessionService) {
        this.authService = authService;
        this.sessionService = sessionService;
    }

    @GetMapping("/auth/testlogin")
    public String testLoginForm(Model model) {
        model.addAttribute("loginForm", new LoginForm());
        return "auth/testlogin";
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("loginForm", new LoginForm());
        model.addAttribute("screen", "auth/login");
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(HttpSession session, @Valid @ModelAttribute("loginForm") LoginForm loginForm, BindingResult bindingResult, @RequestParam(required = false) String redirect, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "입력값을 확인해주세요.");
            return "redirect:" + safeRedirect(redirect, "/login");
        }
        try {
            authService.login(session, loginForm);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:" + safeRedirect(redirect, "/login");
        }
        return "redirect:" + safeRedirect(redirect, sessionUserRedirect(session));
    }

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("screen", "auth/signup");
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signup() {
        return "redirect:/learning/onboarding";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session, @RequestParam(required = false) String redirect) {
        authService.logout(session);
        return "redirect:" + safeRedirect(redirect, "/login");
    }

    private String safeRedirect(String redirect, String fallback) {
        if(redirect != null && !redirect.isBlank() && redirect.startsWith("/") && !redirect.startsWith("//")) {
            return redirect;
        }
        return fallback;
    }

    private String sessionUserRedirect(HttpSession session) {
        Object value = session.getAttribute(SessionUser.SESSION_KEY);
        if (value instanceof SessionUser sessionUser) {
            return sessionUser.defaultRedirectPath();
        }
        return "/learning";
    }


//
//    @GetMapping("/login")
//    public String loginForm(Model model) {
//        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
//        // SessionUser sessionUser = currentSessionUser();
//        // AuthPageView view = authService.loginForm(sessionUser);
//        // model.addAttribute("view", view);
//        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
//        model.addAttribute("screen", "auth/login");
//        return "auth/login";
//    }
//
//    @PostMapping("/login")
//    public String login(@SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser) {
//        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
//        // if (bindingResult.hasErrors()) { return "auth/login"; }
//        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
//        if (sessionUser != null) {
//            return "redirect:" + sessionUser.defaultRedirectPath();
//        }
//        return "redirect:/learning";
//    }
//
//    @GetMapping("/signup")
//    public String signupForm(Model model) {
//        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
//        // SessionUser sessionUser = currentSessionUser();
//        // AuthPageView view = authService.signupForm(sessionUser);
//        // model.addAttribute("view", view);
//        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
//        model.addAttribute("screen", "auth/signup");
//        return "auth/signup";
//    }
//
//    @PostMapping("/signup")
//    public String signup() {
//        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
//        // if (bindingResult.hasErrors()) { return "auth/signup"; }
//        // SessionUser sessionUser = currentSessionUser();
//        // authService.signup(sessionUser, form);
//        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
//        return "redirect:/learning/onboarding";
//    }
//
//    @PostMapping("/logout")
//    public String logout() {
//        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
//        // if (bindingResult.hasErrors()) { return "/login"; }
//        // SessionUser sessionUser = currentSessionUser();
//        // sessionService.logout(sessionUser, form);
//        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
//        return "redirect:/login";
//    }
}
