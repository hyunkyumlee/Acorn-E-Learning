package com.acorn.elearning.analysis.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalysisWrongAnswerSummary {
    private Integer totalWrongAnswers;
    private Integer openWrongAnswers;
    private Integer solvedWrongAnswers;
    private Integer totalWrongCount;
}
