package com.acorn.elearning.auth.view;

import java.util.Map;

public record TutorialStepView(String title, String status, Map<String, Object> attributes) {
    public static TutorialStepView stub(String title) { return new TutorialStepView(title, "SKELETON", Map.of()); }
}
