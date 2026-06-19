package com.acorn.elearning.common.idempotency;

import java.time.LocalDateTime;

public record IdempotencyToken(String token, String formType, Long userId, LocalDateTime issuedAt) {}
