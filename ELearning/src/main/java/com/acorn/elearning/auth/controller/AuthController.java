package com.acorn.elearning.auth.controller;

import com.acorn.elearning.auth.form.LoginForm;
import com.acorn.elearning.auth.form.SignupForm;
import com.acorn.elearning.auth.service.AuthService;
import com.acorn.elearning.auth.service.OAuthService;
import com.acorn.elearning.auth.service.SessionService;
import com.acorn.elearning.security.RememberMeCookie;
import com.acorn.elearning.security.SessionUser;
import jakarta.servlet.http.HttpServletResponse;
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
    private final OAuthService oAuthService;
    private final RememberMeCookie rememberMeCookie;

    public AuthController(AuthService authService, SessionService sessionService, OAuthService oAuthService, RememberMeCookie rememberMeCookie) {
        this.authService = authService;
        this.sessionService = sessionService;
        this.oAuthService = oAuthService;
        this.rememberMeCookie = rememberMeCookie;
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
            @RequestParam(required = false) String linkPending,
            @RequestParam(required = false) String email,
            Model model) {
        SessionUser sessionUser = currentUser(session);
        if (sessionUser != null) {
            return "redirect:" + safeRedirect(redirect, sessionUser.defaultRedirectPath());
        }

        LoginForm form = new LoginForm();
        if (email != null && !email.isBlank()) {
            form.setEmail(email);
        }

        model.addAttribute("loginForm", form);  // ★ new LoginForm() → form
        model.addAttribute("redirect", redirect);
        model.addAttribute("linkPendingProvider", linkPending);
        model.addAttribute("linkPendingProviderLabel", providerLabel(linkPending));  // ★ 추가
        model.addAttribute("screen", "auth/login");
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(HttpSession session,
                        @Valid @ModelAttribute("loginForm") LoginForm loginForm,
                        BindingResult bindingResult,
                        @RequestParam(required = false) String redirect,
                        HttpServletResponse response,           // [추가]
                        RedirectAttributes redirectAttributes,
                        Model model) {
        if (bindingResult.hasErrors()) { model.addAttribute("redirect", redirect); return "auth/login"; }
        try {
            authService.login(session, loginForm);
            sessionService.getUser(session).ifPresent(u -> {
                oAuthService.consumePendingLink(session, u);
                if (loginForm.isRememberMe()) {                 // [추가] 체크 시에만 영속 쿠키
                    rememberMeCookie.issue(response, u.userId());
                }
            });
        } catch (RuntimeException ex) { /* 기존과 동일 */ }
        return "redirect:" + safeRedirect(redirect, sessionUserRedirect(session));
    }

    /** [추가] linkPending 쿼리값(google/github) → 화면 표시명 */
    private static String providerLabel(String provider) {
        if (provider == null || provider.isBlank()) {
            return null;
        }
        return switch (provider.toLowerCase()) {
            case "google" -> "Google";
            case "github" -> "GitHub";
            default -> provider;
        };
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

    // 로그아웃: HttpServletResponse 추가, 쿠키도 삭제
    @PostMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response,   // [추가]
                         @RequestParam(required = false) String redirect) {
        authService.logout(session);
        rememberMeCookie.clear(response);   // [추가]
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