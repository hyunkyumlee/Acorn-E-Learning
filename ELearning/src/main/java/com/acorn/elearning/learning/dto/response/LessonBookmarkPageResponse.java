package com.acorn.elearning.learning.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record LessonBookmarkPageResponse(long totalCount, int page, int size, List<Item> items) {

    public record Item(
            Long lessonId,
            String lessonTitle,
            Long nodeId,
            String nodeTitle,
            Long subjectId,
            String levelCode,
            LocalDateTime bookmarkedAt) {
    }
}
