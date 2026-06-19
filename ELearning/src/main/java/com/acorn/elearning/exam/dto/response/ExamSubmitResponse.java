package com.acorn.elearning.exam.dto.response;

import java.util.Map;

public record ExamSubmitResponse(String status, Map<String, Object> data) {
    public static ExamSubmitResponse stub() { return new ExamSubmitResponse("SKELETON", Map.of()); }
}
