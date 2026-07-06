package com.acorn.elearning.learning.dto.response;

import java.util.List;

/**
 * GET /api/learning/dashboard 응답.
 * rankingSummary / wrongAnswerSummary는 별도 도메인 read 값(없으면 null).
 */
public record LearningDashboardResponse(
        Profile profile,
        List<SubjectListResponse.Item> subjects,
        RoadmapSummary roadmapSummary,
        ProgressSummary progressSummary,
        Attendance attendance,
        Object rankingSummary,
        Object wrongAnswerSummary) {

    public record Profile(String nickname, String currentLevelCode, String gradeCode, int totalScore) {}

    public record RoadmapSummary(Long subjectId, String subjectCode, int planetCount, int completedPlanets) {}

    public record ProgressSummary(int progressPercent) {}

    /** weekly = 이번 주 월~일(index 0=월 ... 6=일) 출석 여부. */
    public record Attendance(int streakCount, boolean attendedToday, boolean[] weekly) {}
}
