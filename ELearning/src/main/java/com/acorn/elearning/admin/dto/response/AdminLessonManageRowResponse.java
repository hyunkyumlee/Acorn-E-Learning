package com.acorn.elearning.admin.dto.response;

import java.time.LocalDateTime;

public record AdminLessonManageRowResponse(
        Long lessonId,
        String subjectName,
        String curriculumTitle,
        String lessonTitle,
        String levelCode,
        LocalDateTime updatedAt,
        Boolean isActive
) { }
