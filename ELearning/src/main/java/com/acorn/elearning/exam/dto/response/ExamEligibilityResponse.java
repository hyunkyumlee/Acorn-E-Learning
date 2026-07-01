package com.acorn.elearning.exam.dto.response;

import com.acorn.elearning.exam.model.ExamSession;

public record ExamEligibilityResponse(
        boolean eligible,
        Long latestExamId,
        String latestStatus,
        String message
) {
    public static ExamEligibilityResponse from(ExamSession latestExam) {
        return new ExamEligibilityResponse(
                true,
                latestExam == null ? null : latestExam.getExamId(),
                latestExam == null ? null : latestExam.getStatus(),
                "AI 코딩테스트를 시작할 수 있습니다.");
    }
}
