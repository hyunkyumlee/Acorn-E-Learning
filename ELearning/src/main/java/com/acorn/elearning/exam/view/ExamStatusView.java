package com.acorn.elearning.exam.view;

import java.util.Map;

public record ExamStatusView(String title, String status, Map<String, Object> attributes) {
    public static ExamStatusView stub(String title) { return new ExamStatusView(title, "SKELETON", Map.of()); }
}
