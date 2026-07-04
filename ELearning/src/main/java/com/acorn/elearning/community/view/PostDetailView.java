package com.acorn.elearning.community.view;

import java.util.Map;

public record PostDetailView(String title, String status, Map<String, Object> attributes) {
    public static PostDetailView stub(String title) { return new PostDetailView(title, "IMPLEMENTED", Map.of()); }
}
