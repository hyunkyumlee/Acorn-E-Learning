package com.acorn.elearning.admin.view;

import java.util.Map;

public record AdminManagePageView(String title, String status, Map<String, Object> attributes) {
    public static AdminManagePageView stub(String title) { return new AdminManagePageView(title, "SKELETON", Map.of()); }
}
