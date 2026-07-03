package com.acorn.elearning.analysis.model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalysisExamProblemResult {
    private Long answerId;
    private Long examId;
    private Long aiProblemId;
    private Integer problemNo;
    private String prompt;
    private Integer passedCaseCount;
    private Boolean correct;
    private String aiReview;
    private String testCaseResult;
    private LocalDateTime submittedAt;
    private LocalDateTime gradedAt;
}
