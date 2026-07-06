package com.acorn.elearning.practice.dto.response;

import java.util.Map;

public record WrongAnswerDetailResponse(
        String status, Map<String, Object> data
) {
    public static WrongAnswerDetailResponse stub() {
        return new WrongAnswerDetailResponse("SKELETON", Map.of()); }

    public static WrongAnswerDetailResponse from(
            Long wrongAnswerId,
            Long problemId,
            String question,
            String answerText,
            Integer wrongCount,
            String reviewStatus,
            Boolean retryBonusAwarded
    ) {
        return new WrongAnswerDetailResponse(
                "SUCCESS",
                Map.of(
                        "wrongAnswerId", wrongAnswerId,
                        "problemId", problemId,
                        "question", question,
                        "answerText", answerText,
                        "wrongCount", wrongCount,
                        "reviewStatus", reviewStatus,
                        "retryBonusAwarded", retryBonusAwarded
                )
        );
    }
}
