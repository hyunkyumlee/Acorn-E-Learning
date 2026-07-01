package com.acorn.elearning.exam.dto.response;

import java.util.List;

public record ExamCodeRunResponse(
        String status,
        boolean passed,
        int passedCount,
        int totalCount,
        long elapsedMs,
        String message,
        List<TestCaseExecutionResult.CaseResult> cases
) {
    public static ExamCodeRunResponse from(TestCaseExecutionResult result, long elapsedMs) {
        return new ExamCodeRunResponse(
                result.status(),
                result.passed(),
                result.passedCount(),
                result.totalCount(),
                elapsedMs,
                message(result),
                result.cases());
    }

    private static String message(TestCaseExecutionResult result) {
        if ("COMPILE_ERROR".equals(result.status())) {
            return "컴파일 오류가 발생했습니다.";
        }
        if ("SECURITY_VIOLATION".equals(result.status())) {
            return "보안 정책으로 실행이 차단되었습니다.";
        }
        if (result.passed()) {
            return "모든 테스트케이스를 통과했습니다.";
        }
        return "일부 테스트케이스를 통과하지 못했습니다.";
    }
}
