package com.acorn.elearning.payment.dto.response;

import java.util.Map;

public record PremiumAccessResponse(String status, Map<String, Object> data) {
    public static PremiumAccessResponse stub() { return new PremiumAccessResponse("SKELETON", Map.of()); }
}
