package com.acorn.elearning.learning.view;

import java.util.List;

/**
 * 레벨 테스트 문항 표시용 View(LEVEL-001).
 * ⚠️ 정답 여부(is_correct)는 화면 소스에 노출되면 안 되므로 이 View에 포함하지 않는다.
 *    (원본 LevelTestChoice 모델을 template에 직접 넘기지 않는 이유)
 */
public record LevelTestQuestionView(
        Long questionId,
        int questionNo,
        String questionText,
        List<ChoiceView> choices
) {
    /** 선택지 표시용(정답 여부 제외). */
    public record ChoiceView(Long choiceId, String choiceLabel, String choiceText) {}
}
