package com.acorn.elearning.analysis.view;

import java.util.Map;

public record AnalysisDashboardView(String title, String status, Map<String, Object> attributes) {
    public static AnalysisDashboardView stub(String title) { return new AnalysisDashboardView(title, "SKELETON", Map.of()); }
}
