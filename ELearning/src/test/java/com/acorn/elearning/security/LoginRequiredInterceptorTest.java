package com.acorn.elearning.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class LoginRequiredInterceptorTest {

    @Test
    void preHandle_redirects_to_login_with_encoded_current_path_when_enforced() throws Exception {
        LoginRequiredInterceptor interceptor = new LoginRequiredInterceptor(true);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/learning/main");
        request.setQueryString("subjectId=1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals("/login?redirect=%2Flearning%2Fmain%3FsubjectId%3D1", response.getRedirectedUrl());
    }

    @Test
    void preHandle_allows_request_when_security_enforcement_is_disabled() throws Exception {
        LoginRequiredInterceptor interceptor = new LoginRequiredInterceptor(false);

        boolean result = interceptor.preHandle(new MockHttpServletRequest("GET", "/learning"), new MockHttpServletResponse(), new Object());

        assertTrue(result);
    }
}
