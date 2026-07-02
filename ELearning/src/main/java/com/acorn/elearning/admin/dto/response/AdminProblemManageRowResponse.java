package com.acorn.elearning.admin.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminProblemManageRowResponse {
    private Long problemId;
    private Long subjectId;
    private Long nodeId;
    private String subjectName;
    private String curriculumTitle;
    private String problemType;
    private String question;
    private String answerText;
    private String explanation;
    private String difficultyCode;
    private Boolean isActive;
}
