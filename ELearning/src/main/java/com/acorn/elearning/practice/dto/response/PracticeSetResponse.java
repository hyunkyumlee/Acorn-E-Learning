package com.acorn.elearning.practice.dto.response;

import java.util.Map;

public record PracticeSetResponse(String status, Map<String, Object> data) {
    // 1. 스켈레톤
    public static PracticeSetResponse stub() {
        return new PracticeSetResponse("SKELETON", Map.of()); }

    // 2. 성공 응답용
    // 이제 컨트롤러에서 PracticeSetResponse.success(map)으로 호출 가능합니다.
    public static PracticeSetResponse success(Map<String, Object> data) {
        return new PracticeSetResponse("SUCCESS", data);
    }

    // 3. 실패 응답용
    public static PracticeSetResponse fail(String message) {
        return new PracticeSetResponse("FAIL", Map.of("message", message));
    }
}
