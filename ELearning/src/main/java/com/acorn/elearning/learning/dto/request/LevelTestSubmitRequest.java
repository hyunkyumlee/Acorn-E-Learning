package com.acorn.elearning.learning.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

/**
 * POST /api/level-tests/attempts 요청 body.
 * answers = 문항별 제출 답안(객관식은 choiceId, 그 외 submittedAnswer).
 */
public record LevelTestSubmitRequest(
        @NotNull Long subjectId,
        @NotEmpty List<Answer> answers) {

    public record Answer(
            @NotNull Long questionId,
            Long choiceId,
            String submittedAnswer) {}

    /** 채점 서비스가 쓰는 (questionId -> choiceId) 맵으로 변환한다. */
    public Map<Long, Long> toAnswerMap() {
        Map<Long, Long> map = new LinkedHashMap<>();
        if (answers != null) {
            for (Answer a : answers) {
                if (a != null && a.questionId() != null) {
                    map.put(a.questionId(), a.choiceId());
                }
            }
        }
        return map;
    }
}
