package com.acorn.elearning.admin.dto.response;

import java.util.Map;

public record AdminUserPageResponse(String status, Map<String, Object> data) {
    public static AdminUserPageResponse stub() { return new AdminUserPageResponse("SKELETON", Map.of()); }
}
