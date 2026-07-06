package com.acorn.elearning.analysis.model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalysisCodingMistakeStat {
    private String mistakeType;
    private Integer occurrenceCount;
    private Integer affectedExamCount;
    private String samplePrompt;
    private LocalDateTime latestOccurredAt;
}
