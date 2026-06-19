package com.acorn.elearning.user.dto.response;

import java.util.Map;

public record UserProfileResponse(String status, Map<String, Object> data) {
    public static UserProfileResponse stub() { return new UserProfileResponse("SKELETON", Map.of()); }
}
