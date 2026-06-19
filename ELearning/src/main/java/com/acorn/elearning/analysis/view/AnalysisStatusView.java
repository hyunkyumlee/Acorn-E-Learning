package com.acorn.elearning.analysis.view;

import java.util.Map;

public record AnalysisStatusView(String title, String status, Map<String, Object> attributes) {
    public static AnalysisStatusView stub(String title) { return new AnalysisStatusView(title, "SKELETON", Map.of()); }
}
