package com.acorn.elearning.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

class OwnerGuardServiceTest {

    private final OwnerGuardService service = new OwnerGuardService();

    @Test
    void requireOwnerOrAdmin_allows_owner() {
        SessionUser user = new SessionUser(1L, "user@example.com", "사용자", SessionUser.ROLE_USER, false);

        assertDoesNotThrow(() -> service.requireOwnerOrAdmin(1L, user));
    }

    @Test
    void requireOwnerOrAdmin_allows_admin() {
        SessionUser admin = new SessionUser(2L, "admin@example.com", "관리자", SessionUser.ROLE_ADMIN, false);

        assertDoesNotThrow(() -> service.requireOwnerOrAdmin(1L, admin));
    }

    @Test
    void requireOwnerOrAdmin_rejects_other_user_with_polite_message() {
        SessionUser user = new SessionUser(2L, "user@example.com", "사용자", SessionUser.ROLE_USER, false);

        BusinessException exception = assertThrows(BusinessException.class, () -> service.requireOwnerOrAdmin(1L, user));

        assertEquals(ErrorCode.AUTH_FORBIDDEN, exception.errorCode());
        assertEquals("권한이 없습니다.", exception.getMessage());
    }
}
