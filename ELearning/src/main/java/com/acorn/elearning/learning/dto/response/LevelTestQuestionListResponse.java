package com.acorn.elearning.learning.dto.response;

import java.util.Map;

public record LevelTestQuestionListResponse(String status, Map<String, Object> data) {
    public static LevelTestQuestionListResponse stub() { return new LevelTestQuestionListResponse("SKELETON", Map.of()); }
}
