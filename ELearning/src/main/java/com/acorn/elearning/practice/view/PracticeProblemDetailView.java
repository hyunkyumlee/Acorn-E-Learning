package com.acorn.elearning.practice.view;

import java.util.Map;

public record PracticeProblemDetailView(String title, String status, Map<String, Object> attributes) {
    public static PracticeProblemDetailView stub(String title) { return new PracticeProblemDetailView(title, "SKELETON", Map.of()); }
}
