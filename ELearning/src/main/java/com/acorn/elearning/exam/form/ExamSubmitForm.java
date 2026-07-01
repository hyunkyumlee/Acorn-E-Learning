package com.acorn.elearning.exam.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExamSubmitForm {
    @NotBlank
    private String confirm = "SUBMIT";

    private String idempotencyToken;
}
