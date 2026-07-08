package com.acorn.elearning.admin.dto.response;

import java.util.List;

public record AdminStatsResponse(
        Summary summary,
        List<AdminChartPointResponse> dailyLearningChart,
        List<AdminChartPointResponse> subjectCompleteChart,
        List<AdminChartPointResponse> subjectExamScoreChart,
        List<TableRow> tableRows
) {
    public record Summary(
            long totalUsers,
            long activeUsers,
            long learningCount,
            long submissionCount,
            long examAttemptCount
    ) {
    }

    public record TableRow(
            String statDate,
            String subjectName,
            long learningCount,
            long submissionCount,
            long examAttemptCount,
            double averageScore
    ) {
    }
}
