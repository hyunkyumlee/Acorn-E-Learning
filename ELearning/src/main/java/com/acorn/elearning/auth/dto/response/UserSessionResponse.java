package com.acorn.elearning.auth.dto.response;

import java.util.Map;

public record UserSessionResponse(String status, Map<String, Object> data) {
    public static UserSessionResponse stub() { return new UserSessionResponse("SKELETON", Map.of()); }
}
