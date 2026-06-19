package com.acorn.elearning.admin.dto.response;

import java.util.Map;

public record ReportPageResponse(String status, Map<String, Object> data) {
    public static ReportPageResponse stub() { return new ReportPageResponse("SKELETON", Map.of()); }
}
