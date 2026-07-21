package com.acorn.elearning.security;

import com.acorn.elearning.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminRequiredInterceptor implements HandlerInterceptor {
    private final boolean enforce;
    private final AuthService authService;

    public AdminRequiredInterceptor(@Value("${knowva.security.enforce:true}") boolean enforce, AuthService authService) {
        this.enforce = enforce;
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!enforce) {
            return true;
        }

        SessionUser sessionUser = currentUser(request);
        boolean authorized = false;

        if (sessionUser != null && sessionUser.admin()) {
            // 정지됐거나 admin 권한이 회수됐으면 거부 (버그 #7)
            Optional<SessionUser> refreshed = authService.revalidate(sessionUser.userId());
            if (refreshed.isPresent() && refreshed.get().admin()) {
                request.getSession(false).setAttribute(SessionUser.SESSION_KEY, refreshed.get());
                authorized = true;
            } else {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.invalidate();
                }
            }
        }

        if (authorized) {
            return true;
        }

        if (request.getRequestURI().startsWith("/api/")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"관리자 권한이 필요합니다.\","
                            + "\"data\":null,\"error\":{\"code\":\"AUTH-FORBIDDEN\","
                            + "\"detail\":\"관리자 전용 API입니다.\",\"fieldErrors\":[]}}"
            );
            return false;
        }

        response.sendRedirect("/error/403");
        return false;
    }

    private SessionUser currentUser(HttpServletRequest request) {
        if (request.getSession(false) == null) {
            return null;
        }
        Object user = request.getSession(false).getAttribute(SessionUser.SESSION_KEY);
        return user instanceof SessionUser sessionUser ? sessionUser : null;
    }
}
