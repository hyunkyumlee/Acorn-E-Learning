package com.acorn.elearning.admin.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ReportPageResponse(
        List<ReportItem> reports
){
    public record ReportItem(
            Long reportId,
            String reporterNickname,
            String targetType,
            Long targetId,
            String targetSummary,
            String reasonCode,
            String status,
            LocalDateTime createdAt
    ){

    }
}