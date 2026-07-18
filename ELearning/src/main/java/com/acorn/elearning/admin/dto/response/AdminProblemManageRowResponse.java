package com.acorn.elearning.admin.dto.response;

import lombok.Getter;
import lombok.Setter;

import com.acorn.elearning.practice.model.ProblemChoice;
import java.util.List;

@Getter
@Setter
public class AdminProblemManageRowResponse {
    private Long problemId;
    private Long subjectId;
    private Long nodeId;
    private Long lessonId;
    private String lessonTitle;
    private String subjectName;
    private String curriculumTitle;
    private String problemType;
    private String question;
    private String answerText;
    private String explanation;
    private String difficultyCode;
    private Boolean isActive;
    private List<ProblemChoice> choices;
}
