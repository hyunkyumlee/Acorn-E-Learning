package com.acorn.elearning.auth.controller;

import com.acorn.elearning.auth.form.LoginForm;
import com.acorn.elearning.auth.form.SignupForm;
import com.acorn.elearning.auth.service.AuthService;
import com.acorn.elearning.auth.service.OAuthService;
import com.acorn.elearning.auth.service.SessionService;
import com.acorn.elearning.common.exception.BusinessException;   // [추가] 로그인 실패 예외 import
import com.acorn.elearning.learning.mapper.SubjectMapper;        // [추가] 관심 과목 목록(name 표시) 조회용
import com.acorn.elearning.learning.model.Subject;               // [추가]
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

import java.util.List;   // [추가]

@Controller
public class AuthController {

    private final AuthService authService;
    private final SessionService sessionService;
    private final OAuthService oAuthService;
    private final RememberMeCookie rememberMeCookie;
    private final SubjectMapper subjectMapper;   // [추가] 관심 과목 dropdown(name 표시)용

    public AuthController(AuthService authService, SessionService sessionService, OAuthService oAuthService, RememberMeCookie rememberMeCookie, SubjectMapper subjectMapper) {   // [수정] subjectMapper 주입
        this.authService = authService;
        this.sessionService = sessionService;
        this.oAuthService = oAuthService;
        this.rememberMeCookie = rememberMeCookie;
        this.subjectMapper = subjectMapper;   // [추가]
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
    public String login(HttpSession session,
                        @Valid @ModelAttribute("loginForm") LoginForm loginForm,
                        BindingResult bindingResult,
                        @RequestParam(required = false) String redirect,
                        HttpServletResponse response,
                        RedirectAttributes redirectAttributes,
                        Model model) {
        if (bindingResult.hasErrors()) { model.addAttribute("redirect", redirect); return "auth/login"; }
        try {
            authService.login(session, loginForm);
            sessionService.getUser(session).ifPresent(u -> {
                if (loginForm.isRememberMe()) {                 // 체크 시에만 영속 쿠키
                    rememberMeCookie.issue(response, u.userId());
                }
            });
        } catch (BusinessException ex) {                         // [수정] 빈 catch(RuntimeException) → 실패를 화면에 표시
            model.addAttribute("redirect", redirect);           // [추가] redirect 파라미터 유지
            model.addAttribute("screen", "auth/login");         // [추가] 레이아웃용 screen 속성
            model.addAttribute("errorMessage", ex.getMessage());// [추가] login.html의 errorMessage 표시부로 전달
            return "auth/login";                                // [추가] redirect 대신 폼 재렌더(입력값·오류 유지)
        }
        return "redirect:" + safeRedirect(redirect, sessionUserRedirect(session));
    }

    @GetMapping("/signup")
    public String signupForm(@SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser, Model model) {
        if (sessionUser != null) {
            return "redirect:" + sessionUser.defaultRedirectPath();
        }
        model.addAttribute("signupForm", new SignupForm());
        model.addAttribute("screen", "auth/signup");
        addSubjectOptions(model);   // [추가] 관심 과목을 name으로 보여주기 위한 목록
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
            addSubjectOptions(model);   // [추가] 검증 실패로 폼을 다시 그릴 때도 과목 목록 유지
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
    public String logout(HttpSession session, HttpServletResponse response,
                         @RequestParam(required = false) String redirect) {
        authService.logout(session);
        rememberMeCookie.clear(response);
        return "redirect:" + safeRedirect(redirect, "/login");
    }

    // [추가] 회원가입 화면의 "관심 과목" 을 ID 입력이 아니라 name dropdown 으로 보여주기 위한 활성 과목 목록
    private void addSubjectOptions(Model model) {
        List<Subject> subjects = subjectMapper.findAll().stream()
                .filter(s -> Boolean.TRUE.equals(s.getIsActive()))   // 활성 과목만 노출
                .toList();
        model.addAttribute("subjects", subjects);
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