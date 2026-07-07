package com.acorn.elearning.learning.view;

/** 온보딩 결과 화면 표시값. startPlanetNo = 출발 지점(BRONZE=1/SILVER=2/GOLD=3). */
public record OnboardingResultView(
        String resultLevelCode,
        int correctCount,
        int totalCount,
        boolean fromTest,
        int startPlanetNo
) {
}
