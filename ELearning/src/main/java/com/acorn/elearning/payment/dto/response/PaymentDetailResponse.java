package com.acorn.elearning.payment.dto.response;

import java.util.Map;

public record PaymentDetailResponse(String status, Map<String, Object> data) {
    public static PaymentDetailResponse stub() { return new PaymentDetailResponse("SKELETON", Map.of()); }
}
