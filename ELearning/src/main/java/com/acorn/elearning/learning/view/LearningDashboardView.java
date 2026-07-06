package com.acorn.elearning.learning.view;

/**
 * 학습 메인 대시보드 표시용 View (사용자 프로필/출석).
 * 데이터 출처:
 *   - nickname        : SessionUser (로그인 세션)
 *   - primarySubjectId, currentLevelCode, gradeCode, totalScore : user_learning_profiles
 *   - streakCount, attendedToday : attendance_records (최근 1건)
 * 로드맵 진행률(행성 완료수·%)은 선택 과목 기준으로 ProgressService.RoadmapProgress가 별도 제공한다.
 */
public record LearningDashboardView(
        String nickname,
        Long primarySubjectId,
        String currentLevelCode,
        String gradeCode,
        int totalScore,
        int streakCount,
        boolean attendedToday
) {}
