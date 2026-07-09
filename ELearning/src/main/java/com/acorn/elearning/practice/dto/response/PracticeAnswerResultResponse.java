package com.acorn.elearning.practice.dto.response;

import java.util.Map;

public record PracticeAnswerResultResponse(String status, Map<String, Object> data) {
    public static PracticeAnswerResultResponse stub() {
        return new PracticeAnswerResultResponse("SKELETON", Map.of());
    }

    // 채점 결과 반환
    public static PracticeAnswerResultResponse from(int correctCount, int totalCount) {
        return new PracticeAnswerResultResponse("SUCCESS", Map.of(
                "correctCount", correctCount,
                "totalCount", totalCount
        ));
    }
}
