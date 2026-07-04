package com.acorn.elearning.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class GuestOnlyInterceptor implements HandlerInterceptor {
    private final boolean enforce;

    public GuestOnlyInterceptor(@Value("${knowva.security.enforce:true}") boolean enforce) {
        this.enforce = enforce;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        SessionUser sessionUser = currentUser(request);
        if (!enforce || sessionUser == null) {
            return true;
        }
        response.sendRedirect(sessionUser.defaultRedirectPath());
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
