package com.acorn.elearning.exam.dto.response;

import com.acorn.elearning.exam.model.ExamSession;

public record ExamSubmitResponse(
        Long examId,
        String status,
        String resultStatus,
        Integer correctCount,
        Integer totalProblemCount
) {
    public static ExamSubmitResponse from(ExamSession session) {
        return new ExamSubmitResponse(
                session.getExamId(),
                session.getStatus(),
                session.getResultStatus(),
                session.getCorrectCount(),
                session.getTotalProblemCount());
    }
}
