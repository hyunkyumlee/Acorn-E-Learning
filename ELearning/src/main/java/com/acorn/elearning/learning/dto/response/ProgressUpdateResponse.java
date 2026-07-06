package com.acorn.elearning.learning.dto.response;

import com.acorn.elearning.learning.view.LessonProgressView;

/**
 * POST /api/lessons/{lessonId}/complete 응답: 이론 완료 처리 결과.
 * nextAction = START_PRACTICE / NEXT_NODE / GATE.
 */
public record ProgressUpdateResponse(
        Long nodeId,
        boolean lessonCompleted,
        int progressRate,
        String nextAction) {

    public static ProgressUpdateResponse from(LessonProgressView view) {
        return new ProgressUpdateResponse(
                view.nodeId(), view.lessonCompleted(), view.progressRate(), view.nextAction());
    }
}
