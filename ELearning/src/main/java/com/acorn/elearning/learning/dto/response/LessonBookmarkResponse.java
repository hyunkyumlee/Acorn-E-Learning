package com.acorn.elearning.learning.dto.response;

import java.util.Map;

public record LessonBookmarkResponse(String status, Map<String, Object> data) {
    public static LessonBookmarkResponse stub() { return new LessonBookmarkResponse("SKELETON", Map.of()); }
}
