package com.acorn.elearning.learning.dto.response;

import java.util.Map;

public record LessonDetailResponse(String status, Map<String, Object> data) {
    public static LessonDetailResponse stub() { return new LessonDetailResponse("SKELETON", Map.of()); }
}
