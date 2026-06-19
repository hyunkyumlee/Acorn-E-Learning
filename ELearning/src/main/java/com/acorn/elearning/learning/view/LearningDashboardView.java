package com.acorn.elearning.learning.view;

import java.util.Map;

public record LearningDashboardView(String title, String status, Map<String, Object> attributes) {
    public static LearningDashboardView stub(String title) { return new LearningDashboardView(title, "SKELETON", Map.of()); }
}
