package com.acorn.elearning.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {
    private final boolean enforce;
    public LoginRequiredInterceptor(@Value("${knowva.security.enforce:false}") boolean enforce) { this.enforce = enforce; }
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Object user = request.getSession(false) == null ? null : request.getSession(false).getAttribute(SessionUser.SESSION_KEY);
        if (!enforce || user instanceof SessionUser) return true;
        response.sendRedirect("/login?redirect=" + request.getRequestURI());
        return false;
    }
}
