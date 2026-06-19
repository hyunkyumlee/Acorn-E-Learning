package com.acorn.elearning.analysis.dto.response;

import java.util.Map;

public record AnalysisStatusResponse(String status, Map<String, Object> data) {
    public static AnalysisStatusResponse stub() { return new AnalysisStatusResponse("SKELETON", Map.of()); }
}
