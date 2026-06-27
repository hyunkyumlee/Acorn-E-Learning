package com.acorn.elearning.common.idempotency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

class IdempotencyTokenServiceTest {

    private final IdempotencyTokenService service = new IdempotencyTokenService();

    @Test
    void consume_returns_true_once_when_token_was_issued() {
        MockHttpSession session = new MockHttpSession();
        IdempotencyToken token = service.issue("PAY-002", session, 1L);

        assertTrue(service.consume(token.token(), session));
        assertFalse(service.consume(token.token(), session));
    }

    @Test
    void requireAndConsume_throws_polite_business_error_when_token_is_missing() {
        MockHttpSession session = new MockHttpSession();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.requireAndConsume("", "request-hash", session));

        assertEquals(ErrorCode.COMMON_IDEMPOTENCY_KEY_REQUIRED, exception.errorCode());
        assertEquals("중복 방지 토큰이 필요합니다.", exception.getMessage());
    }
}
