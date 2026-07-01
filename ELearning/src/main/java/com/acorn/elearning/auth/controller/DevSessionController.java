package com.acorn.elearning.auth.controller;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.security.SessionUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DevSessionController {
    // TODO Auth 1번 실제 로그인/session 구현이 완료되면 이 controller 전체를 반드시 삭제해야 합니다.
    private final boolean enabled;

    public DevSessionController(@Value("${knowva.dev-session.enabled:false}") boolean enabled) {
        this.enabled = enabled;
    }

    @GetMapping("/dev/session/user")
    public String user(HttpSession httpSession, @RequestParam(defaultValue = "/exams/coding-test") String redirect) {
        requireEnabled();
        httpSession.setAttribute(
                SessionUser.SESSION_KEY,
                new SessionUser(2L, "learner@knowva.local", "누비학습자", SessionUser.ROLE_USER, false));
        return "redirect:" + safeRedirect(redirect);
    }

    @GetMapping("/dev/session/premium")
    public String premium(HttpSession httpSession, @RequestParam(defaultValue = "/exams/coding-test") String redirect) {
        requireEnabled();
        httpSession.setAttribute(
                SessionUser.SESSION_KEY,
                new SessionUser(3L, "premium@knowva.local", "프리미엄학습자", SessionUser.ROLE_USER, true));
        return "redirect:" + safeRedirect(redirect);
    }

    @GetMapping("/dev/session/admin")
    public String admin(HttpSession httpSession, @RequestParam(defaultValue = "/admin") String redirect) {
        requireEnabled();
        httpSession.setAttribute(
                SessionUser.SESSION_KEY,
                new SessionUser(1L, "admin@knowva.local", "관리자", SessionUser.ROLE_ADMIN, false));
        return "redirect:" + safeRedirect(redirect);
    }

    @PostMapping("/dev/session/clear")
    public String clear(HttpSession httpSession) {
        requireEnabled();
        httpSession.invalidate();
        return "redirect:/login";
    }

    private void requireEnabled() {
        if (!enabled) {
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN, "임시 개발 세션 endpoint가 비활성화되어 있습니다.");
        }
    }

    private String safeRedirect(String redirect) {
        if (redirect == null || redirect.isBlank() || !redirect.startsWith("/") || redirect.startsWith("//")) {
            return "/exams/coding-test";
        }
        return redirect;
    }
}
