package com.acorn.elearning.admin.dto.response;

import java.util.Map;

public record AdminCommunityActionResponse(String status, Map<String, Object> data) {
    public static AdminCommunityActionResponse stub() { return new AdminCommunityActionResponse("SKELETON", Map.of()); }
}
