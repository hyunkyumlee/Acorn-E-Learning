    package com.acorn.elearning.learning.model;


    import lombok.Getter;
    import lombok.Setter;

    @Getter
    @Setter
    public class LevelTestChoice {
        private Long choiceId;
private Long questionId;
private String choiceLabel;
private String choiceText;
private Boolean isCorrect;
private Integer sortOrder;
    }
