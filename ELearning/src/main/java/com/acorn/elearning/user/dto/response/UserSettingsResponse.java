package com.acorn.elearning.user.dto.response;

import java.util.Map;

public record UserSettingsResponse(String status, Map<String, Object> data) {
    public static UserSettingsResponse stub() { return new UserSettingsResponse("SKELETON", Map.of()); }
}
