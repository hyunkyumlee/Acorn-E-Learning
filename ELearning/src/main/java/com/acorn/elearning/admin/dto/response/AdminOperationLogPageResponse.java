package com.acorn.elearning.admin.dto.response;

import java.util.Map;

public record AdminOperationLogPageResponse(String status, Map<String, Object> data) {
    public static AdminOperationLogPageResponse stub() { return new AdminOperationLogPageResponse("SKELETON", Map.of()); }
}
