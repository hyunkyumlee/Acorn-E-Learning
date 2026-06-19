package com.acorn.elearning.user.dto.response;

import java.util.Map;

public record MyPageSummaryResponse(String status, Map<String, Object> data) {
    public static MyPageSummaryResponse stub() { return new MyPageSummaryResponse("SKELETON", Map.of()); }
}
