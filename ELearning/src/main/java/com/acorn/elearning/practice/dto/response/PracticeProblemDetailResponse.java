package com.acorn.elearning.practice.dto.response;

import java.util.Map;

public record PracticeProblemDetailResponse(String status, Map<String, Object> data) {
    public static PracticeProblemDetailResponse stub() { return new PracticeProblemDetailResponse("SKELETON", Map.of()); }
}
