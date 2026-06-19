package com.acorn.elearning.community.dto.response;

import java.util.Map;

public record ReactionResponse(String status, Map<String, Object> data) {
    public static ReactionResponse stub() { return new ReactionResponse("SKELETON", Map.of()); }
}
