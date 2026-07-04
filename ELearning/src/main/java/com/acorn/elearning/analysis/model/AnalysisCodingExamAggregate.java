package com.acorn.elearning.analysis.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalysisCodingExamAggregate {
    private Integer totalExamCount;
    private Integer passedExamCount;
    private Integer totalProblemCount;
    private Integer correctCount;
}
