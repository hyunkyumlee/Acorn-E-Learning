package com.acorn.elearning.exam.dto.response;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;

public record ExamProblemStepResponse(
        Long examId,
        Integer currentProblemNo,
        Integer previousProblemNo,
        Integer nextProblemNo,
        Integer totalProblemCount,
        Integer answeredCount,
        boolean firstProblem,
        boolean lastProblem,
        boolean allAnswered,
        boolean submittableAfterCurrentSave,
        ExamSessionResponse.Problem problem
) {
    public static ExamProblemStepResponse from(ExamSessionResponse exam, Integer problemNo) {
        ExamSessionResponse.Problem problem = exam.problems().stream()
                .filter(candidate -> candidate.problemNo().equals(problemNo))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "AI 시험 문제를 찾을 수 없습니다."));
        int answeredCount = (int) exam.problems().stream()
                .filter(ExamSessionResponse.Problem::answered)
                .count();
        Integer previousProblemNo = problem.problemNo() > 1 ? problem.problemNo() - 1 : null;
        Integer nextProblemNo = problem.problemNo() < exam.totalProblemCount() ? problem.problemNo() + 1 : null;
        int answeredCountAfterCurrentSave = problem.answered() ? answeredCount : answeredCount + 1;
        return new ExamProblemStepResponse(
                exam.examId(),
                problem.problemNo(),
                previousProblemNo,
                nextProblemNo,
                exam.totalProblemCount(),
                answeredCount,
                previousProblemNo == null,
                nextProblemNo == null,
                answeredCount == exam.totalProblemCount(),
                answeredCountAfterCurrentSave == exam.totalProblemCount(),
                problem);
    }
}
