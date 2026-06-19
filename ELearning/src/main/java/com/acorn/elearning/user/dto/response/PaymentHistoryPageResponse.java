package com.acorn.elearning.user.dto.response;

import java.util.Map;

public record PaymentHistoryPageResponse(String status, Map<String, Object> data) {
    public static PaymentHistoryPageResponse stub() { return new PaymentHistoryPageResponse("SKELETON", Map.of()); }
}
