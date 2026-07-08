    package com.acorn.elearning.practice.model;

    import java.time.LocalDateTime;
    import lombok.Getter;
    import lombok.Setter;

    @Getter
    @Setter
    public class PracticeSubmission {
        private Long submissionId;
        private Long setAttemptId;
        private Long setItemId;
        private Long userId;
        private Long problemId;
        private String submissionContext;
        private String submittedAnswer;
        private Boolean isCorrect;
        private Boolean isSkipped;
        private LocalDateTime solvedAt;
        private LocalDateTime createdAt;
    }
