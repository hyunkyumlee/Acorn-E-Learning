package com.acorn.elearning.exam.view;

import java.util.Map;

public record ExamResultView(String title, String status, Map<String, Object> attributes) {
    public static ExamResultView stub(String title) { return new ExamResultView(title, "SKELETON", Map.of()); }
}
