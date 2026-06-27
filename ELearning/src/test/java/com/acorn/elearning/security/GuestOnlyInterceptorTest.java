package com.acorn.elearning.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class GuestOnlyInterceptorTest {

    @Test
    void preHandle_redirects_admin_to_admin_main_when_enforced() throws Exception {
        GuestOnlyInterceptor interceptor = new GuestOnlyInterceptor(true);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/login");
        request.getSession(true).setAttribute(
                SessionUser.SESSION_KEY,
                new SessionUser(1L, "admin@example.com", "관리자", SessionUser.ROLE_ADMIN, false));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals("/admin", response.getRedirectedUrl());
    }

    @Test
    void preHandle_allows_guest_when_enforced() throws Exception {
        GuestOnlyInterceptor interceptor = new GuestOnlyInterceptor(true);

        boolean result = interceptor.preHandle(new MockHttpServletRequest("GET", "/login"), new MockHttpServletResponse(), new Object());

        assertTrue(result);
    }
}
