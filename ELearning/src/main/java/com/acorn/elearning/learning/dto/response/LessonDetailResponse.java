package com.acorn.elearning.learning.dto.response;

/**
 * GET /api/lessons/{lessonId} 응답: 이론 본문 + 북마크 여부 + 단원 진행.
 */
public record LessonDetailResponse(
        Long lessonId,
        Long nodeId,
        String title,
        String content,
        String exampleCode,
        boolean bookmarked,
        Progress progress) {

    /** 이 레슨이 속한 단원의 진행 상태. progressRate는 0~100 정수. */
    public record Progress(boolean lessonCompleted, boolean practicePassed, int progressRate) {}
}
