package com.acorn.elearning.payment.dto.response;

public record TossPaymentOrderResponse(
        Long paymentId,
        String orderId,
        String orderName,
        int amount,
        String customerKey
) {}
