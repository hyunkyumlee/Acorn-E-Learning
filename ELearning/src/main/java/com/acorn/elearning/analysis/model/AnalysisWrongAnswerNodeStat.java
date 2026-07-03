package com.acorn.elearning.analysis.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalysisWrongAnswerNodeStat {
    private Long nodeId;
    private String nodeTitle;
    private Integer wrongAnswerCount;
    private Integer totalWrongCount;
    private Integer openCount;
}
