package com.acorn.elearning.payment.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRefund {
    private Long refundId;
    private Long paymentId;
    private Long userId;
    private String refundStatus;
    private BigDecimal refundAmount;
    private String refundReason;
    private String pgProvider;
    private String pgRefundTransactionId;
    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;
    private LocalDateTime failedAt;
    private String failureCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
