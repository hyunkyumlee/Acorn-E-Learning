package com.acorn.elearning.learning.dto.response;

import java.util.Map;

public record LevelTestResultResponse(String status, Map<String, Object> data) {
    public static LevelTestResultResponse stub() { return new LevelTestResultResponse("SKELETON", Map.of()); }
}
