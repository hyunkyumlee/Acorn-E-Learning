package com.acorn.elearning.analysis.form;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GenerateAnalysisForm {
    @NotNull
    private Long examId;
}
