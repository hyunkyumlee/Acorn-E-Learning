package com.acorn.elearning.auth.dto.response;

import java.util.Map;

public record SocialAccountResponse(String status, Map<String, Object> data) {
    public static SocialAccountResponse stub() { return new SocialAccountResponse("SKELETON", Map.of()); }
}
