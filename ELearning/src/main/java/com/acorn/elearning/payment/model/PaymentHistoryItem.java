package com.acorn.elearning.payment.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentHistoryItem {
    private Long paymentId;
    private String orderNo;
    private Long userId;
    private Long productId;
    private String productCode;
    private String productName;
    private String paymentMethod;
    private String paymentStatus;
    private String pgProvider;
    private String pgTransactionId;
    private BigDecimal amount;
    private LocalDateTime paidAt;
    private LocalDateTime paymentCreatedAt;
    private LocalDateTime paymentUpdatedAt;
    private Long grantId;
    private String grantType;
    private String grantStatus;
    private LocalDateTime grantedAt;
    private LocalDateTime expiresAt;
    private Long refundId;
    private String refundStatus;
    private BigDecimal refundAmount;
    private String refundReason;
    private String refundPgProvider;
    private String pgRefundTransactionId;
    private LocalDateTime refundRequestedAt;
    private LocalDateTime refundCompletedAt;
    private LocalDateTime refundFailedAt;
    private String refundFailureCode;
}
