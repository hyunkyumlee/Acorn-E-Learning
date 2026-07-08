package com.acorn.elearning.learning.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLessonProgress {
    private Long lessonProgressId;
    private Long userId;
    private Long lessonId;
    private Boolean theoryCompleted;
    private Boolean practicePassed;
    private BigDecimal progressRate;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
