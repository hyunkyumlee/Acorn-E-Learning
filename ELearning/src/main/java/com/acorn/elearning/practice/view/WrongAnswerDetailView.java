package com.acorn.elearning.practice.view;

import java.util.Map;

public record WrongAnswerDetailView(String title, String status, Map<String, Object> attributes) {
    public static WrongAnswerDetailView stub(String title) { return new WrongAnswerDetailView(title, "SKELETON", Map.of()); }
}
