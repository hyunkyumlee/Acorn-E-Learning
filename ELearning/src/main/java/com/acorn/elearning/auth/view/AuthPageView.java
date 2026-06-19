package com.acorn.elearning.auth.view;

import java.util.Map;

public record AuthPageView(String title, String status, Map<String, Object> attributes) {
    public static AuthPageView stub(String title) { return new AuthPageView(title, "SKELETON", Map.of()); }
}
