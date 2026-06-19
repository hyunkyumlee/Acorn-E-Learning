package com.acorn.elearning.auth.view;

import java.util.Map;

public record UserSessionView(String title, String status, Map<String, Object> attributes) {
    public static UserSessionView stub(String title) { return new UserSessionView(title, "SKELETON", Map.of()); }
}
