package com.acorn.elearning.practice.dto.response;

import java.util.Map;

public record PracticeAnswerResultResponse(String status, Map<String, Object> data) {
    public static PracticeAnswerResultResponse stub() { return new PracticeAnswerResultResponse("SKELETON", Map.of()); }
}
