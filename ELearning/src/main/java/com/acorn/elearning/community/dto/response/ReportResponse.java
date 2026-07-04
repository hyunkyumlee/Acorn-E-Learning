package com.acorn.elearning.community.dto.response;

import com.acorn.elearning.community.model.Report;

public record ReportResponse(Long reportId, String targetType, Long targetId, String status) {
    public static ReportResponse from(Report report) {
        return new ReportResponse(report.getReportId(), report.getTargetType(), report.getTargetId(), report.getStatus());
    }
}
