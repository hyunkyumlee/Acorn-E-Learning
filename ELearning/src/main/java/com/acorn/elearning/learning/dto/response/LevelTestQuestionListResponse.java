package com.acorn.elearning.learning.dto.response;

import com.acorn.elearning.learning.view.LevelTestQuestionView;
import java.util.List;

/**
 * GET /api/level-tests/questions 응답: 레벨 테스트 문항 목록(정답 미노출).
 * totalCount = 문항 수(레벨 테스트는 8문항).
 */
public record LevelTestQuestionListResponse(int totalCount, List<LevelTestQuestionView> questions) {

    public static LevelTestQuestionListResponse of(List<LevelTestQuestionView> questions) {
        return new LevelTestQuestionListResponse(questions.size(), questions);
    }
}
