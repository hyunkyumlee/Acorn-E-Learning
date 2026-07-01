package com.acorn.elearning.auth.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.security.SessionUser;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

class DevSessionControllerTest {

    @Test
    void premium_sets_temporary_premium_session_when_dev_endpoint_is_enabled() {
        DevSessionController controller = new DevSessionController(true);
        MockHttpSession session = new MockHttpSession();

        String redirect = controller.premium(session, "/analysis");

        SessionUser sessionUser = (SessionUser) session.getAttribute(SessionUser.SESSION_KEY);
        assertEquals("redirect:/analysis", redirect);
        assertEquals(3L, sessionUser.userId());
        assertEquals("premium@knowva.local", sessionUser.email());
        assertEquals(SessionUser.ROLE_USER, sessionUser.role());
        assertEquals(true, sessionUser.premiumActive());
    }

    @Test
    void user_blocks_temporary_session_when_dev_endpoint_is_disabled() {
        DevSessionController controller = new DevSessionController(false);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> controller.user(new MockHttpSession(), "/exams/coding-test"));

        assertEquals(ErrorCode.AUTH_FORBIDDEN, exception.errorCode());
        assertEquals("임시 개발 세션 endpoint가 비활성화되어 있습니다.", exception.getMessage());
    }
}
