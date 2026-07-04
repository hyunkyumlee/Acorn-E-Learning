package com.acorn.elearning.analysis.dto.response;

public record AnalysisAutoRefreshResponse(
        boolean attempted,
        boolean refreshed,
        AnalysisReportResponse report
) {
    public static AnalysisAutoRefreshResponse skipped(AnalysisReportResponse report) {
        return new AnalysisAutoRefreshResponse(false, false, report);
    }

    public static AnalysisAutoRefreshResponse attempted(AnalysisReportResponse report) {
        return new AnalysisAutoRefreshResponse(true, "SUCCESS".equals(report.status()), report);
    }
}
