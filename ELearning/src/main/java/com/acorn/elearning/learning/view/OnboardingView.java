package com.acorn.elearning.learning.view;

import java.util.Map;

public record OnboardingView(String title, String status, Map<String, Object> attributes) {
    public static OnboardingView stub(String title) { return new OnboardingView(title, "SKELETON", Map.of()); }
}
