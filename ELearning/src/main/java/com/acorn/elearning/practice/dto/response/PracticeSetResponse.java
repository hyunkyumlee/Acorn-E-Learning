package com.acorn.elearning.practice.dto.response;

import java.util.Map;

public record PracticeSetResponse(String status, Map<String, Object> data) {
    public static PracticeSetResponse stub() { return new PracticeSetResponse("SKELETON", Map.of()); }
}
