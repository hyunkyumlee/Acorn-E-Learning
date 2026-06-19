    package com.acorn.elearning.practice.model;

    import java.time.LocalDateTime;
    import lombok.Getter;
    import lombok.Setter;

    @Getter
    @Setter
    public class WrongAnswer {
        private Long wrongAnswerId;
private Long userId;
private Long problemId;
private Long lastSubmissionId;
private Integer wrongCount;
private String reviewStatus;
private Boolean retryBonusAwarded;
private LocalDateTime createdAt;
private LocalDateTime updatedAt;
    }
