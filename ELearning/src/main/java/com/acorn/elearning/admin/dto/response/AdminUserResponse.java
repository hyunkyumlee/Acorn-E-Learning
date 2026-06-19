package com.acorn.elearning.admin.dto.response;

import java.util.Map;

public record AdminUserResponse(String status, Map<String, Object> data) {
    public static AdminUserResponse stub() { return new AdminUserResponse("SKELETON", Map.of()); }
}
