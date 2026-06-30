package com.acorn.elearning.learning.view;

/**
 * 학습 메인(SR-003) 대시보드 표시용 View.
 * 데이터 출처:
 *   - nickname        : SessionUser (로그인 세션)
 *   - primarySubjectId, currentLevelCode, gradeCode, totalScore : user_learning_profiles
 *   - progressRate    : learning_progress (주 과목 노드 progress_rate 평균, 0~100 정수)
 *   - streakCount, attendedToday : attendance_records (최근 1건)
 */
public record LearningDashboardView(
        String nickname,
        Long primarySubjectId,
        String currentLevelCode,
        String gradeCode,
        int totalScore,
        int progressRate,
        int streakCount,
        boolean attendedToday
) {}
