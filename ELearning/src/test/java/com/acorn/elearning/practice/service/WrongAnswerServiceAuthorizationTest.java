package com.acorn.elearning.practice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.practice.form.WrongAnswerRetryForm;
import org.junit.jupiter.api.Test;

class WrongAnswerServiceAuthorizationTest {

    private final WrongAnswerService service = new WrongAnswerService(null, null, null, null, null);

    @Test
    void summary_rejects_null_session_user() {
        BusinessException exception = assertThrows(BusinessException.class, () -> service.summary(null));
        assertEquals(ErrorCode.AUTH_REQUIRED, exception.errorCode());
    }

    @Test
    void list_rejects_null_session_user() {
        BusinessException exception = assertThrows(BusinessException.class, () -> service.list(null, 1L, null));
        assertEquals(ErrorCode.AUTH_REQUIRED, exception.errorCode());
    }

    @Test
    void detail_rejects_null_session_user() {
        BusinessException exception = assertThrows(BusinessException.class, () -> service.detail(null, 1L));
        assertEquals(ErrorCode.AUTH_REQUIRED, exception.errorCode());
    }

    @Test
    void note_rejects_null_session_user() {
        BusinessException exception = assertThrows(BusinessException.class, () -> service.note(null, 1L));
        assertEquals(ErrorCode.AUTH_REQUIRED, exception.errorCode());
    }

    @Test
    void retry_rejects_null_session_user() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.retry(null, new WrongAnswerRetryForm(), 1L));
        assertEquals(ErrorCode.AUTH_REQUIRED, exception.errorCode());
    }

    @Test
    void markReviewed_rejects_null_session_user() {
        BusinessException exception = assertThrows(BusinessException.class, () -> service.markReviewed(null, 1L));
        assertEquals(ErrorCode.AUTH_REQUIRED, exception.errorCode());
    }
}
