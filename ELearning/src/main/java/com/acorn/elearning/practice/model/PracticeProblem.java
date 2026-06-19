    package com.acorn.elearning.practice.model;

    import java.time.LocalDateTime;
    import lombok.Getter;
    import lombok.Setter;

    @Getter
    @Setter
    public class PracticeProblem {
        private Long problemId;
private Long subjectId;
private Long nodeId;
private String problemType;
private String question;
private String answerText;
private String difficultyCode;
private Long createdBy;
private Boolean isActive;
private LocalDateTime createdAt;
private LocalDateTime updatedAt;
    }
