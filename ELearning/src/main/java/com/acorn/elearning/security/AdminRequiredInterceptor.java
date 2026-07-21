package com.acorn.elearning.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminRequiredInterceptor implements HandlerInterceptor {
    private final boolean enforce;

    public AdminRequiredInterceptor(@Value("${knowva.security.enforce:true}") boolean enforce) {
        this.enforce = enforce;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        SessionUser sessionUser = currentUser(request);
        if (!enforce || (sessionUser != null && sessionUser.admin())) {
            return true;
        }

        if (request.getRequestURI().startsWith("/api/")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"\uAD00\uB9AC\uC790 \uAD8C\uD55C\uC774 \uD544\uC694\uD569\uB2C8\uB2E4.\","
                            + "\"data\":null,\"error\":{\"code\":\"AUTH-FORBIDDEN\","
                            + "\"detail\":\"\uAD00\uB9AC\uC790 \uC804\uC6A9 API\uC785\uB2C8\uB2E4.\",\"fieldErrors\":[]}}"
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
