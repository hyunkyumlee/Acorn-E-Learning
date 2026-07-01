package com.acorn.elearning.common.idempotency;

import java.io.Serializable;
import java.time.LocalDateTime;

public record IdempotencyToken(String token, String formType, Long userId, LocalDateTime issuedAt) implements Serializable {
    private static final long serialVersionUID = 1L;
}
