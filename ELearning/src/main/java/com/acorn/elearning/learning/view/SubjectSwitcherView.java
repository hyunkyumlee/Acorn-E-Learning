package com.acorn.elearning.learning.view;

import java.util.Map;

public record SubjectSwitcherView(String title, String status, Map<String, Object> attributes) {
    public static SubjectSwitcherView stub(String title) { return new SubjectSwitcherView(title, "SKELETON", Map.of()); }
}
