package com.acorn.elearning.learning.view;

import java.util.Map;

public record RoadmapView(String title, String status, Map<String, Object> attributes) {
    public static RoadmapView stub(String title) { return new RoadmapView(title, "SKELETON", Map.of()); }
}
