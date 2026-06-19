package com.acorn.elearning.admin.dto.response;

import java.util.Map;

public record AdminPostPageResponse(String status, Map<String, Object> data) {
    public static AdminPostPageResponse stub() { return new AdminPostPageResponse("SKELETON", Map.of()); }
}
