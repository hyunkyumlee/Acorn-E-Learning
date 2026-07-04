package com.acorn.elearning.practice.view;

import java.util.Map;

public record WrongAnswerSummaryView(String title, String status, Map<String, Object> attributes) {

    public static WrongAnswerSummaryView stub(String title) {
        return new WrongAnswerSummaryView(title, "SKELETON", Map.of());
    }

    public static WrongAnswerSummaryView from(int total, int pending, int solved) {
     return new WrongAnswerSummaryView(
             "오답 복습",
             "READY",
             Map.of(
                     "totalWrongCount", total,
                     "pendingCount", pending,
                     "solvedCount", solved
             )
     );
 }
}