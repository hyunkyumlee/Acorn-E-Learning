package com.acorn.elearning.learning.model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSubjectEnrollment {

    private Long enrollmentId;
    private Long userId;
    private Long subjectId;
    private String status;
    private String startMode;
    private LocalDateTime enrolledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
