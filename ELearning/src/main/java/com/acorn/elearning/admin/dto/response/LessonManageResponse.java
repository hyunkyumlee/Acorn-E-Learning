package com.acorn.elearning.admin.dto.response;

import java.util.Map;

public record LessonManageResponse(String status, Map<String, Object> data) {
    public static LessonManageResponse stub() { return new LessonManageResponse("SKELETON", Map.of()); }
}
