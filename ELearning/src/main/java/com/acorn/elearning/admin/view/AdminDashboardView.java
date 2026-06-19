package com.acorn.elearning.admin.view;

import java.util.Map;

public record AdminDashboardView(String title, String status, Map<String, Object> attributes) {
    public static AdminDashboardView stub(String title) { return new AdminDashboardView(title, "SKELETON", Map.of()); }
}
