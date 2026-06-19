package com.acorn.elearning.payment.dto.response;

import java.util.Map;

public record PaymentResultResponse(String status, Map<String, Object> data) {
    public static PaymentResultResponse stub() { return new PaymentResultResponse("SKELETON", Map.of()); }
}
