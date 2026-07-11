package com.acorn.elearning.security;

import com.acorn.elearning.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


// 세션에 로그인 유저가 없고 REMEMBER_ME 쿠키가 유효하면 세션 복원 (자동 로그인)
@Component
public class RememberMeInterceptor implements HandlerInterceptor {
    private final RememberMeCookie rememberMeCookie;
    private final AuthService authService;

    public RememberMeInterceptor(RememberMeCookie rememberMeCookie, AuthService authService) {
        this.rememberMeCookie = rememberMeCookie;
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession(false);  //세션 없으면 생성 안 함
        if (session != null && session.getAttribute(SessionUser.SESSION_KEY) != null) {
            return true; // 이미 로그인
        }
        RememberMeCookie.Token token = rememberMeCookie.resolve(request);
        if (token != null) {
            authService.restoreSession(request.getSession(), token.userId(), token.version()); // 복원할 때만 세션 생성
        }
        return true;
    }
}