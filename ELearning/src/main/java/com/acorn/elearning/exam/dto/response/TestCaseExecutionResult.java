package com.acorn.elearning.exam.dto.response;

import java.util.List;

public record TestCaseExecutionResult(
        String status,
        boolean passed,
        int passedCount,
        int totalCount,
        List<CaseResult> cases
) {
    public record CaseResult(
            String input,
            String expectedOutput,
            String actualOutput,
            boolean passed,
            String errorMessage
    ) {}
}
