package com.acorn.elearning.practice.view;

import java.util.Map;

public record WrongAnswerDetailView(String title, String status, Map<String, Object> attributes) {
    public static WrongAnswerDetailView stub(String title) {
        return new WrongAnswerDetailView(title, "SKELETON", Map.of());
    }

    public static WrongAnswerDetailView from(
            Long wrongAnswerId,
            Long problemId,
            String question,
            String answerText,
            String explanation,
            Long lessonId,
            Long nodeId,
            Integer wrongCount,
            String reviewStatus,
            Boolean retryBonusAwarded
    ) {
        return new WrongAnswerDetailView(
                "오답 상세",
                "READY",
                Map.of(
                        "wrongAnswerId", wrongAnswerId,
                        "problemId", problemId,
                        "question", question,
                        "answerText", answerText,
                        "explanation", explanation,
                        "lessonId", lessonId,
                        "nodeId", nodeId,
                        "wrongCount", wrongCount,
                        "reviewStatus", reviewStatus,
                        "retryBonusAwarded", retryBonusAwarded
                )
        );
    }
}
