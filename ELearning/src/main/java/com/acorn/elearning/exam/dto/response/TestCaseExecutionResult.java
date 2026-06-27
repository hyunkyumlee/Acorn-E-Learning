package com.acorn.elearning.exam.dto.response;

import java.util.List;
import java.util.Map;

public record TestCaseExecutionResult(
        String status,
        boolean passed,
        int passedCount,
        int totalCount,
        List<Map<String, Object>> cases) {
    public static TestCaseExecutionResult skeleton() {
        return new TestCaseExecutionResult("SKELETON", false, 0, 0, List.of());
    }
}
