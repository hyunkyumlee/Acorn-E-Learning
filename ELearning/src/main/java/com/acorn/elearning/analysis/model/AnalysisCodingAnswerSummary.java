package com.acorn.elearning.analysis.model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalysisCodingAnswerSummary {
    private Long examId;
    private String subjectName;
    private String levelCode;
    private Integer problemNo;
    private String prompt;
    private String starterCode;
    private String submittedCode;
    private Integer passedCaseCount;
    private Boolean correct;
    private String aiReview;
    private LocalDateTime gradedAt;
}
