    package com.acorn.elearning.learning.model;

    import java.time.LocalDateTime;
    import lombok.Getter;
    import lombok.Setter;

    @Getter
    @Setter
    public class LevelTestAttempt {
        private Long attemptId;
private Long userId;
private Long subjectId;
private Integer totalCount;
private Integer correctCount;
private String resultLevelCode;
private String status;
private LocalDateTime submittedAt;
private LocalDateTime createdAt;
    }
