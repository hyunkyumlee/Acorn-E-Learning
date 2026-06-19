package com.acorn.elearning.learning.view;

import java.util.Map;

public record LessonDetailView(String title, String status, Map<String, Object> attributes) {
    public static LessonDetailView stub(String title) { return new LessonDetailView(title, "SKELETON", Map.of()); }
}
