package com.acorn.elearning.learning.view;

import java.util.Map;

public record LevelTestResultView(String title, String status, Map<String, Object> attributes) {
    public static LevelTestResultView stub(String title) { return new LevelTestResultView(title, "SKELETON", Map.of()); }
}
