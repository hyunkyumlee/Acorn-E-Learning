    package com.acorn.elearning.learning.model;

    import java.time.LocalDateTime;
    import lombok.Getter;
    import lombok.Setter;

    @Getter
    @Setter
    public class LevelTestAnswer {
        private Long answerId;
private Long attemptId;
private Long questionId;
private Long choiceId;
private String submittedAnswer;
private Boolean isCorrect;
private LocalDateTime createdAt;
    }
