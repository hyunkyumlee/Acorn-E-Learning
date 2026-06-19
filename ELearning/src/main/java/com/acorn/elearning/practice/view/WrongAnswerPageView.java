package com.acorn.elearning.practice.view;

import java.util.Map;

public record WrongAnswerPageView(String title, String status, Map<String, Object> attributes) {
    public static WrongAnswerPageView stub(String title) { return new WrongAnswerPageView(title, "SKELETON", Map.of()); }
}
