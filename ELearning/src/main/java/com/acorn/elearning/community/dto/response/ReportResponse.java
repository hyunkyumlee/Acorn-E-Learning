package com.acorn.elearning.community.dto.response;

import java.util.Map;

public record ReportResponse(String status, Map<String, Object> data) {
    public static ReportResponse stub() { return new ReportResponse("SKELETON", Map.of()); }
}
