package com.acorn.elearning.practice.dto.response;

import java.util.Map;

public record PracticeSetResponse(String status, Map<String, Object> data) {
    // 1. 기존의 스켈레톤(테스트용) 응답
    public static PracticeSetResponse stub() {
        return new PracticeSetResponse("SKELETON", Map.of()); }

    // 2. [추가] 성공 응답을 위한 정적 팩토리 메서드
    // 이제 컨트롤러에서 PracticeSetResponse.success(map)으로 호출 가능합니다.
    public static PracticeSetResponse success(Map<String, Object> data) {
        return new PracticeSetResponse("SUCCESS", data);
    }

    // 3. [추가] 실패 응답을 위한 정적 팩토리 메서드 (필요 시)
    public static PracticeSetResponse fail(String message) {
        return new PracticeSetResponse("FAIL", Map.of("message", message));
    }
}
