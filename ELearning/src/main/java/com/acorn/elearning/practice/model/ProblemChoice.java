    package com.acorn.elearning.practice.model;


    import lombok.Getter;
    import lombok.Setter;

    @Getter
    @Setter
    public class ProblemChoice {
        private Long choiceId;
private Long problemId;
private String choiceLabel;
private String choiceText;
private Boolean isCorrect;
private Integer sortOrder;
    }
