package com.acorn.elearning.admin.form;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProblemForm {
    private Long problemId;
    private Long subjectId;
    private Long nodeId;
    private String problemType;
    private String question;
    private String answerText;
    private String difficultyCode;
    private Boolean isActive;
    private String choices;
    private String explanation;
}
