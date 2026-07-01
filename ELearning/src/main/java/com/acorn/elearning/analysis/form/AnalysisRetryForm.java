package com.acorn.elearning.analysis.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalysisRetryForm {
    @NotBlank
    private String confirm = "RETRY";
}
