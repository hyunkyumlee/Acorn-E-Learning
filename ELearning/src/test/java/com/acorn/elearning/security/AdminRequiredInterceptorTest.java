package com.acorn.elearning.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.acorn.elearning.auth.service.AuthService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class AdminRequiredInterceptorTest {

    @Test
    void preHandle_allows_admin_when_enforced() throws Exception {
        SessionUser adminUser = new SessionUser(1L, "admin@example.com", "admin", SessionUser.ROLE_ADMIN, false);
        AuthService authService = mock(AuthService.class);
        when(authService.revalidate(1L)).thenReturn(Optional.of(adminUser));
        AdminRequiredInterceptor interceptor = new AdminRequiredInterceptor(true, authService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin");
        request.getSession(true).setAttribute(SessionUser.SESSION_KEY, adminUser);

        boolean result = interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertTrue(result);
    }

    @Test
    void preHandle_redirects_user_to_forbidden_page_when_enforced() throws Exception {
        AdminRequiredInterceptor interceptor = new AdminRequiredInterceptor(true, mock(AuthService.class));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin");
        request.getSession(true).setAttribute(
                SessionUser.SESSION_KEY,
                new SessionUser(2L, "user@example.com", "user", SessionUser.ROLE_USER, false));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals("/error/403", response.getRedirectedUrl());
    }

    @Test
    void preHandle_returns_json_403_for_api_request_from_regular_user() throws Exception {
        AdminRequiredInterceptor interceptor = new AdminRequiredInterceptor(true, mock(AuthService.class));
        MockHttpServletRequest request = new MockHttpServletRequest("PATCH", "/api/admin/community/posts/1/status");
        request.getSession(true).setAttribute(
                SessionUser.SESSION_KEY,
                new SessionUser(2L, "user@example.com", "user", SessionUser.ROLE_USER, false));
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(403, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        assertTrue(response.getContentAsString().contains("AUTH-FORBIDDEN"));
    }
}
