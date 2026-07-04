package com.acorn.elearning.auth.controller;

import com.acorn.elearning.auth.form.LoginForm;
import com.acorn.elearning.auth.form.SignupForm;
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

    //testhtml 용
    @GetMapping("/auth/testlogin")
    public String testLoginForm(HttpSession session, Model model) {
        model.addAttribute("loginForm", new LoginForm());
        model.addAttribute("sessionUser", currentUser(session));
        return "auth/testlogin";
    }
    @GetMapping("/auth/testsignup")
    public String testSignupForm(HttpSession session, Model model) {
        model.addAttribute("signupForm", new SignupForm());
        model.addAttribute("sessionUser", currentUser(session));
        return "auth/testsignup";
    }
    //------------

    @GetMapping("/login")
    public String loginForm(
            HttpSession session,
            @RequestParam(required = false) String redirect,
            Model model) {
        SessionUser sessionUser = currentUser(session);
        if (sessionUser != null) {
            return "redirect:" + safeRedirect(redirect, sessionUser.defaultRedirectPath());
        }
        model.addAttribute("loginForm", new LoginForm());
        model.addAttribute("redirect", redirect);
        model.addAttribute("screen", "auth/login");
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(
            HttpSession session,
            @Valid @ModelAttribute("loginForm") LoginForm loginForm,
            BindingResult bindingResult,
            @RequestParam(required = false) String redirect,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("redirect", redirect);
            return "auth/login";
        }
        try {
            authService.login(session, loginForm);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            if (redirect != null && !redirect.isBlank()) {
                redirectAttributes.addAttribute("redirect", redirect);
            }
            return "redirect:/login";
        }
        // redirect 없으면 role별 home — admin → /admin
        return "redirect:" + safeRedirect(redirect, sessionUserRedirect(session));
    }

    @GetMapping("/signup")
    public String signupForm(@SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser, Model model) {
        if (sessionUser != null) {
            return "redirect:" + sessionUser.defaultRedirectPath();
        }
        model.addAttribute("signupForm", new SignupForm());
        model.addAttribute("screen", "auth/signup");
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signup(
            HttpSession session,
            @Valid @ModelAttribute("signupForm") SignupForm signupForm,
            BindingResult bindingResult,
            @RequestParam(required = false) String redirect,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/signup";
        }
        try {
            authService.signup(session, signupForm);
            redirectAttributes.addFlashAttribute("successMessage", "회원가입이 완료되었습니다. 로그인해 주세요.");
            return "redirect:" + safeRedirect(redirect, "/login");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/signup";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session, @RequestParam(required = false) String redirect) {
        authService.logout(session);
        return "redirect:" + safeRedirect(redirect, "/login");
    }

    private SessionUser currentUser(HttpSession session) {
        Object value = session.getAttribute(SessionUser.SESSION_KEY);
        return value instanceof SessionUser u ? u : null;
    }

    private String safeRedirect(String redirect, String fallback) {
        if (redirect != null && !redirect.isBlank()
                && redirect.startsWith("/") && !redirect.startsWith("//")) {
            return redirect;
        }
        return fallback;
    }

    private String sessionUserRedirect(HttpSession session) {
        SessionUser sessionUser = currentUser(session);
        if (sessionUser != null) {
            return sessionUser.defaultRedirectPath();
        }
        return "/learning";
    }
}