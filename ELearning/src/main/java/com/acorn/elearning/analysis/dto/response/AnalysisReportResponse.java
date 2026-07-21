package com.acorn.elearning.analysis.dto.response;

import com.acorn.elearning.analysis.model.AiAnalysisReport;

public record AnalysisReportResponse(
        Long reportId,
        Long examId,
        String status,
        String freeSummary,
        String premiumDetail,
        String analysisErrorCode,
        Integer retryCount
) {
    public static AnalysisReportResponse from(
            AiAnalysisReport report,
            boolean premiumActive
    ) {
        return new AnalysisReportResponse(
                report.getReportId(),
                report.getExamId(),
                report.getStatus(),
                report.getFreeSummary(),
                premiumActive ? report.getPremiumDetail() : null,
                report.getAnalysisErrorCode(),
                report.getRetryCount());
    }
}
