package com.acorn.elearning.exam.dto.response;

import com.acorn.elearning.exam.model.ExamSession;
import com.acorn.elearning.exam.service.ExamLearningScopeService.ExamLearningEligibility;

public record ExamEligibilityResponse(
        boolean eligible,
        Long latestExamId,
        String latestStatus,
        String message,
        int incompleteRequiredLessonCount
) {
    public static ExamEligibilityResponse from(ExamSession latestExam, ExamLearningEligibility eligibility) {
        return new ExamEligibilityResponse(
                eligibility.eligible(),
                latestExam == null ? null : latestExam.getExamId(),
                latestExam == null ? null : latestExam.getStatus(),
                eligibility.message(),
                eligibility.incompleteRequiredLessonCount());
    }
}
