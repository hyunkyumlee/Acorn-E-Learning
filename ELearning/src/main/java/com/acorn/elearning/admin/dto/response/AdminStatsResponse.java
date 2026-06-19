package com.acorn.elearning.admin.dto.response;

import java.util.Map;

public record AdminStatsResponse(String status, Map<String, Object> data) {
    public static AdminStatsResponse stub() { return new AdminStatsResponse("SKELETON", Map.of()); }
}
