package com.acorn.elearning.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class AdminRequiredInterceptorTest {

    @Test
    void preHandle_allows_admin_when_enforced() throws Exception {
        AdminRequiredInterceptor interceptor = new AdminRequiredInterceptor(true);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin");
        request.getSession(true).setAttribute(
                SessionUser.SESSION_KEY,
                new SessionUser(1L, "admin@example.com", "관리자", SessionUser.ROLE_ADMIN, false));

        boolean result = interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertTrue(result);
    }

    @Test
    void preHandle_redirects_user_to_forbidden_page_when_enforced() throws Exception {
        AdminRequiredInterceptor interceptor = new AdminRequiredInterceptor(true);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin");
        request.getSession(true).setAttribute(
                SessionUser.SESSION_KEY,
                new SessionUser(2L, "user@example.com", "사용자", SessionUser.ROLE_USER, false));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals("/error/403", response.getRedirectedUrl());
    }
}
