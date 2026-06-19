package com.acorn.elearning.payment.dto.response;

import java.util.Map;

public record PaymentProductListResponse(String status, Map<String, Object> data) {
    public static PaymentProductListResponse stub() { return new PaymentProductListResponse("SKELETON", Map.of()); }
}
