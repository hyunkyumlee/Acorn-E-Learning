package com.acorn.elearning.learning.dto.response;

import java.util.Map;

public record ProgressUpdateResponse(String status, Map<String, Object> data) {
    public static ProgressUpdateResponse stub() { return new ProgressUpdateResponse("SKELETON", Map.of()); }
}
