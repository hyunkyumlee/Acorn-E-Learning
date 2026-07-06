package com.acorn.elearning.learning.dto.response;

import com.acorn.elearning.learning.view.LevelTestResultView;
import java.util.List;

/**
 * POST /api/level-tests/attempts 응답: 채점 결과 + 해금된 레벨 목록.
 * 등급 = 정답 수 기준 0-2 BRONZE / 3-5 SILVER / 6-8 GOLD.
 */
public record LevelTestResultResponse(
        Long attemptId,
        int totalCount,
        int correctCount,
        String resultLevelCode,
        List<String> unlockedLevels) {

    public static LevelTestResultResponse of(LevelTestResultView view, List<String> unlockedLevels) {
        return new LevelTestResultResponse(
                view.attemptId(), view.totalCount(), view.correctCount(),
                view.resultLevelCode(), unlockedLevels);
    }
}
