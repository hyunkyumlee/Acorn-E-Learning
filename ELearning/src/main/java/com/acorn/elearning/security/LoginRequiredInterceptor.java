package com.acorn.elearning.security;

import com.acorn.elearning.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {
    private final boolean enforce;
    private final AuthService authService;

    public LoginRequiredInterceptor(@Value("${knowva.security.enforce:true}") boolean enforce, AuthService authService) {
        this.enforce = enforce;
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!enforce) {
            return true;
        }

        SessionUser sessionUser = currentUser(request);
        if (sessionUser == null) {
            response.sendRedirect("/login?redirect=" + encodedCurrentPath(request));
            return false;
        }

        // 정지된 계정이면 세션 무효화 후 로그인으로 (버그 #7: 세션이 DB 최신 상태를 반영하지 않던 문제)
        Optional<SessionUser> refreshed = authService.revalidate(sessionUser.userId());
        if (refreshed.isEmpty()) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect("/login?redirect=" + encodedCurrentPath(request));
            return false;
        }
        request.getSession(false).setAttribute(SessionUser.SESSION_KEY, refreshed.get());

        return true;
    }

    private SessionUser currentUser(HttpServletRequest request) {
        if (request.getSession(false) == null) {
            return null;
        }
        Object user = request.getSession(false).getAttribute(SessionUser.SESSION_KEY);
        return user instanceof SessionUser sessionUser ? sessionUser : null;
    }

    private String encodedCurrentPath(HttpServletRequest request) {
        String queryString = request.getQueryString();
        String currentPath = queryString == null
                ? request.getRequestURI()
                : request.getRequestURI() + "?" + queryString;
        return URLEncoder.encode(currentPath, StandardCharsets.UTF_8);
    }

}
