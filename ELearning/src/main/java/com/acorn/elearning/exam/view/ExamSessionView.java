package com.acorn.elearning.exam.view;

import java.util.Map;

public record ExamSessionView(String title, String status, Map<String, Object> attributes) {
    public static ExamSessionView stub(String title) { return new ExamSessionView(title, "SKELETON", Map.of()); }
}
