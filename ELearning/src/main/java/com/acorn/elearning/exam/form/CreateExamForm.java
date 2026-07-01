package com.acorn.elearning.exam.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateExamForm {
    @NotNull
    private Long subjectId;

    @NotBlank
    private String levelCode;

    private String idempotencyToken;
}
