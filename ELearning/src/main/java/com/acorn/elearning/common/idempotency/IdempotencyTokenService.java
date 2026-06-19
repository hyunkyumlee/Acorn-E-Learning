package com.acorn.elearning.common.idempotency;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Map;
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
    public boolean consume(String tokenValue, HttpSession session) { return tokenValue != null && tokens(session).remove(tokenValue) != null; }
    @SuppressWarnings("unchecked")
    private Map<String, IdempotencyToken> tokens(HttpSession session) {
        Object value = session.getAttribute(SESSION_KEY);
        if (value instanceof Map<?, ?>) return (Map<String, IdempotencyToken>) value;
        Map<String, IdempotencyToken> created = new ConcurrentHashMap<>();
        session.setAttribute(SESSION_KEY, created);
        return created;
    }
}
