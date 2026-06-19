package com.acorn.elearning.practice.view;

import java.util.Map;

public record PracticeSetView(String title, String status, Map<String, Object> attributes) {
    public static PracticeSetView stub(String title) { return new PracticeSetView(title, "SKELETON", Map.of()); }
}
