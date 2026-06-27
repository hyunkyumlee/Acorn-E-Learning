package com.acorn.elearning.common.idempotency;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class IdempotencyTokenService {
    private static final String SESSION_KEY = "KNOWVA_IDEMPOTENCY_TOKENS";

    public IdempotencyToken issue(String formType, HttpSession session, Long userId) {
        String value = UUID.randomUUID().toString();
        IdempotencyToken token = new IdempotencyToken(value, formType, userId, LocalDateTime.now());
        tokens(session).put(value, token);
        return token;
    }

    public boolean consume(String tokenValue, HttpSession session) {
        return hasText(tokenValue) && tokens(session).remove(tokenValue) != null;
    }

    public void requireAndConsume(String tokenValue, String requestHash, HttpSession session) {
        if (!hasText(tokenValue)) {
            throw new BusinessException(ErrorCode.COMMON_IDEMPOTENCY_KEY_REQUIRED);
        }
        if (tokens(session).remove(tokenValue) == null) {
            throw new BusinessException(ErrorCode.COMMON_IDEMPOTENCY_CONFLICT);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, IdempotencyToken> tokens(HttpSession session) {
        Objects.requireNonNull(session, "HttpSession은 필수입니다.");
        Object value = session.getAttribute(SESSION_KEY);
        if (value instanceof Map<?, ?>) {
            return (Map<String, IdempotencyToken>) value;
        }
        Map<String, IdempotencyToken> created = new ConcurrentHashMap<>();
        session.setAttribute(SESSION_KEY, created);
        return created;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
