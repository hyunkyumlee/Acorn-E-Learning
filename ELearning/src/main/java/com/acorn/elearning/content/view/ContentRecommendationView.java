package com.acorn.elearning.content.view;

import java.util.Map;

public record ContentRecommendationView(String title, String status, Map<String, Object> attributes) {
    public static ContentRecommendationView stub(String title) { return new ContentRecommendationView(title, "IMPLEMENTED", Map.of()); }
}
