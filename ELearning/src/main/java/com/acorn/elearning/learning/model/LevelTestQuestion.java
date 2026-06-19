    package com.acorn.elearning.learning.model;

    import java.time.LocalDateTime;
    import lombok.Getter;
    import lombok.Setter;

    @Getter
    @Setter
    public class LevelTestQuestion {
        private Long questionId;
private Long subjectId;
private Integer questionNo;
private String questionText;
private String questionType;
private String explanation;
private String difficultyCode;
private Boolean isActive;
private LocalDateTime createdAt;
private LocalDateTime updatedAt;
    }
