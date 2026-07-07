package com.acorn.elearning.learning.view;

/** 온보딩 진행 중 선택 요약(화면 표시용). */
public record OnboardingProfileView(
        String nickname,
        Long subjectId,
        String subjectName,
        String learningGoal
) {
}
