package com.acorn.elearning.analysis.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalysisSubjectSummary {
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private Integer codingExamCount;
    private Integer passedExamCount;
    private Integer codingCorrectProblems;
    private Integer codingTotalProblems;
    private Integer failedProblemCount;
    private Integer retryCount;
}
