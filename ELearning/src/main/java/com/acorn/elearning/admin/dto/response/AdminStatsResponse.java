package com.acorn.elearning.admin.dto.response;

import java.util.List;

public record AdminStatsResponse(
        List<TableRow> tableRows
){
    public record TableRow(
            String statDate,
            String subjectName,
            long learningCount,
            long submissionCount,
            long examAttemptCount,
            double averageScore
    ){
    }

}
