package com.acorn.elearning.exam.view;

import java.util.Map;

public record ExamEligibilityView(String title, String status, Map<String, Object> attributes) {
    public static ExamEligibilityView stub(String title) { return new ExamEligibilityView(title, "SKELETON", Map.of()); }
}
