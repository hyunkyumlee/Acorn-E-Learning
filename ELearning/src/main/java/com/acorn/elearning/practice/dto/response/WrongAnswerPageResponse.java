package com.acorn.elearning.practice.dto.response;

import java.util.Map;

public record WrongAnswerPageResponse(String status, Map<String, Object> data) {
    public static WrongAnswerPageResponse stub() { return new WrongAnswerPageResponse("SKELETON", Map.of()); }
}
