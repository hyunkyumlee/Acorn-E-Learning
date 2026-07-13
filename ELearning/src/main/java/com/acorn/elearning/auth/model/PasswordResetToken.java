package com.acorn.elearning.auth.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PasswordResetToken {
    private Long tokenId;
    private Long userId;
    private String tokenHash; // 토큰 원문의 SHA-256 hex
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt; //null 이면 아직 유효, 값이 있으면 사용(무효화)됨
    private LocalDateTime createdAt;
}
