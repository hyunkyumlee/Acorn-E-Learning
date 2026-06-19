package com.acorn.elearning.practice.view;

import java.util.Map;

public record PracticeFeedbackView(String title, String status, Map<String, Object> attributes) {
    public static PracticeFeedbackView stub(String title) { return new PracticeFeedbackView(title, "SKELETON", Map.of()); }
}
