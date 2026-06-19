package com.acorn.elearning.practice.view;

import java.util.Map;

public record WrongAnswerSummaryView(String title, String status, Map<String, Object> attributes) {
    public static WrongAnswerSummaryView stub(String title) { return new WrongAnswerSummaryView(title, "SKELETON", Map.of()); }
}
