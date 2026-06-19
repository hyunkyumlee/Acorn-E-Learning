package com.acorn.elearning.user.view;

import java.util.Map;

public record SettingsHomeView(String title, String status, Map<String, Object> attributes) {
    public static SettingsHomeView stub(String title) { return new SettingsHomeView(title, "SKELETON", Map.of()); }
}
