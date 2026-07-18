package com.acorn.elearning.admin.form;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProblemForm {
    private Long problemId;
    private Long subjectId;
    private Long lessonId;
    private Long nodeId;
    private String problemType;
    private String question;
    private String answerText;
    private String difficultyCode;
    private Boolean isActive;
    private String choices;
    private List<String> choiceTexts;
    private Integer correctChoiceNumber;
    private String explanation;
}
