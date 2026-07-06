package com.acorn.elearning.common.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AiGeneratedTextSanitizerTest {

    @Test
    void removeStarterCodePraise_removes_only_starter_code_praise() {
        String sanitized = AiGeneratedTextSanitizer.removeStarterCodePraise(
                "좋은 시작입니다. Scanner 입력은 올바르게 작성하셨습니다. "
                        + "초기 코딩테스트에서는 기본적인 메서드 작성 가능성을 보여주셨습니다. "
                        + "출력문을 추가하셔야 합니다.");

        assertEquals("출력문을 추가하셔야 합니다.", sanitized);
    }

    @Test
    void removeStarterCodePraise_keeps_user_logic_praise() {
        String sanitized = AiGeneratedTextSanitizer.removeStarterCodePraise(
                "정답입니다. 반복문으로 합계를 잘 계산했습니다.");

        assertEquals("정답입니다. 반복문으로 합계를 잘 계산했습니다.", sanitized);
    }

    @Test
    void cleanUserFacingAiText_replaces_development_terms() {
        String sanitized = AiGeneratedTextSanitizer.cleanUserFacingAiText(
                "문제 요구사항을 확인하세요. TODO를 지워 주세요. 구현 예시를 참고하세요.");

        assertEquals("문제 조건을 확인하세요.", sanitized);
    }
}
