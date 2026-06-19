package com.acorn.elearning.practice.dto.response;

import java.util.Map;

public record WrongAnswerDetailResponse(String status, Map<String, Object> data) {
    public static WrongAnswerDetailResponse stub() { return new WrongAnswerDetailResponse("SKELETON", Map.of()); }
}
