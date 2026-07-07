package com.acorn.elearning.admin.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminLessonManageRowResponse {
    private Long lessonId;
    private Long nodeId;
    private String subjectName;
    private String curriculumTitle;
    private String lessonTitle;
    private String content;
    private String levelCode;
    private Integer sortOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive;
}
