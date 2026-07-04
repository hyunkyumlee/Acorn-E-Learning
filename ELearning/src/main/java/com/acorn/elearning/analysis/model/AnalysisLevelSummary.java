package com.acorn.elearning.analysis.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalysisLevelSummary {
    private String levelCode;
    private Integer codingExamCount;
    private Integer passedExamCount;
    private Integer codingCorrectProblems;
    private Integer codingTotalProblems;
    private Integer failedProblemCount;
    private Integer retryCount;
}
