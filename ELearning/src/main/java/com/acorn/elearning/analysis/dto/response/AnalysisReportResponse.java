package com.acorn.elearning.analysis.dto.response;

import java.util.Map;

public record AnalysisReportResponse(String status, Map<String, Object> data) {
    public static AnalysisReportResponse stub() { return new AnalysisReportResponse("SKELETON", Map.of()); }
}
