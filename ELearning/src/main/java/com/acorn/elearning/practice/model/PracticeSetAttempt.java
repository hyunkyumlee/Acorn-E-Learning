    package com.acorn.elearning.practice.model;

    import java.time.LocalDateTime;
    import lombok.Getter;
    import lombok.Setter;

    @Getter
    @Setter
    public class PracticeSetAttempt {
        private Long setAttemptId;
        private Long userId;
        private Long subjectId; //추가
        private Long nodeId;
        private Integer totalCount;
        private Integer correctCount;
        private String status;
        private Boolean passed;
        private LocalDateTime completedAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
