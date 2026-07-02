package com.acorn.elearning.analysis.dto.response;

import com.acorn.elearning.analysis.model.AiAnalysisReport;

public record AnalysisStatusResponse(
        Long reportId,
        Long examId,
        String status,
        String analysisErrorCode,
        Integer retryCount
) {
    public static AnalysisStatusResponse from(AiAnalysisReport report) {
        return new AnalysisStatusResponse(
                report.getReportId(),
                report.getExamId(),
                report.getStatus(),
                report.getAnalysisErrorCode(),
                report.getRetryCount());
    }
}
