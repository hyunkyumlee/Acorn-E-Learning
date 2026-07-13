package com.acorn.elearning.payment.dto.toss;

public record TossPaymentApproveResponse(
        String paymentKey,
        String orderId,
        String status,
        Integer totalAmount
) {}
