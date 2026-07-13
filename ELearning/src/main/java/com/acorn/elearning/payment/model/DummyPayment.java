package com.acorn.elearning.payment.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DummyPayment {
    private Long paymentId;
    private String orderNo;
    private Long userId;
    private Long productId;
    private String paymentMethod;
    private String paymentStatus;
    private String pgProvider;
    private String pgTransactionId;
    private BigDecimal amount;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
