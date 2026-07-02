package com.acorn.elearning.exam.dto.response;

import com.acorn.elearning.exam.model.ExamSession;

public record ExamStatusResponse(
        Long examId,
        String status,
        String resultStatus,
        Integer correctCount,
        Integer totalProblemCount,
        Integer retryCount
) {
    public static ExamStatusResponse from(ExamSession session) {
        return new ExamStatusResponse(
                session.getExamId(),
                session.getStatus(),
                session.getResultStatus(),
                session.getCorrectCount(),
                session.getTotalProblemCount(),
                session.getRetryCount());
    }
}
