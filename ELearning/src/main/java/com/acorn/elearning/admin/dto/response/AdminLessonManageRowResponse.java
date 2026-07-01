package com.acorn.elearning.admin.dto.response;

import java.time.LocalDateTime;

public record AdminLessonManageRowResponse(
        Long lessonId,
        Long nodeId,
        String subjectName,
        String curriculumTitle,
        String lessonTitle,
        String content,
        String levelCode,
        Integer sortOrder,
        LocalDateTime updatedAt,
        Boolean isActive
) { }
