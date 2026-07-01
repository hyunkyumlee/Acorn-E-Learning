package com.acorn.elearning.learning.view;

/**
 * 레벨 테스트 결과 표시용 View(LEVEL-002, SR-004 결과).
 * 채점 결과 = 정답 개수(0~8) 기준 등급: 0-2 Bronze / 3-5 Silver / 6-8 Gold.
 * 해설은 표시하지 않는다(해설은 문제풀이/AI 영역 — 매핑표 기준).
 */
public record LevelTestResultView(
        Long attemptId,
        Long subjectId,
        String resultLevelCode,
        int correctCount,
        int totalCount
) {}
