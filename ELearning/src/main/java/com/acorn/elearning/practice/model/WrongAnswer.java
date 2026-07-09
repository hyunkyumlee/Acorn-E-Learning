    package com.acorn.elearning.practice.model;

    import java.time.LocalDateTime;
    import lombok.Getter;
    import lombok.Setter;

    @Getter
    @Setter
    public class WrongAnswer {
        // 1. 개별 오답 기록 식별 고유 ID
        private Long wrongAnswerId;

        // 2. 오답이 어떤 문제 세트(Attempt)에서 나왔는지 알려주는 FK
        private Long setAttemptId;

        private Long userId;
        private Long problemId;
        private Long lastSubmissionId;
        private Integer wrongCount;
        private String reviewStatus;
        private Boolean retryBonusAwarded;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
