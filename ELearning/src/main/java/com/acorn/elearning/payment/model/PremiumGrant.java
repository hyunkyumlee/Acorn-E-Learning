package com.acorn.elearning.payment.model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PremiumGrant {
    private Long grantId;
    private Long userId;
    private Long paymentId;
    private String grantType;
    private String status;
    private LocalDateTime grantedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
