        package com.acorn.elearning.learning.model;

        import java.math.BigDecimal;
import java.time.LocalDateTime;
        import lombok.Getter;
        import lombok.Setter;

        @Getter
        @Setter
        public class LearningProgress {
            private Long progressId;
    private Long userId;
    private Long subjectId;
    private Long nodeId;
    private Boolean lessonCompleted;
    private Boolean practicePassed;
    private BigDecimal progressRate;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
        }
