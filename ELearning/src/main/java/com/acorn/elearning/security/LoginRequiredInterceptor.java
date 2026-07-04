package com.acorn.elearning.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {
    private final boolean enforce;

    public LoginRequiredInterceptor(@Value("${knowva.security.enforce:true}") boolean enforce) {
        this.enforce = enforce;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!enforce || currentUser(request) != null) {
            return true;
        }
        response.sendRedirect("/login?redirect=" + encodedCurrentPath(request));
        return false;
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
