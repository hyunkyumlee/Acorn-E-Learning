package com.acorn.elearning.learning.view;

/**
 * 과목 소개 화면의 레벨 한 줄 요약(게이트 제외).
 *
 * @param planetCount 해당 레벨의 행성 수
 * @param lessonCount 해당 레벨 행성들의 레슨 수 합계
 */
public record SubjectLevelSummary(String levelCode, int planetCount, int lessonCount) {}
