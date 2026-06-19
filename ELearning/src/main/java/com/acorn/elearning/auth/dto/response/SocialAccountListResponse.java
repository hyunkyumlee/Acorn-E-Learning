package com.acorn.elearning.auth.dto.response;

import java.util.Map;

public record SocialAccountListResponse(String status, Map<String, Object> data) {
    public static SocialAccountListResponse stub() { return new SocialAccountListResponse("SKELETON", Map.of()); }
}
