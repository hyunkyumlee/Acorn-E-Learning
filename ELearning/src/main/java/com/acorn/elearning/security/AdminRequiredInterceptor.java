package com.acorn.elearning.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminRequiredInterceptor implements HandlerInterceptor {
    private final boolean enforce;

    public AdminRequiredInterceptor(@Value("${knowva.security.enforce:false}") boolean enforce) {
        this.enforce = enforce;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        SessionUser sessionUser = currentUser(request);
        if (!enforce || sessionUser != null && sessionUser.admin()) {
            return true;
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
