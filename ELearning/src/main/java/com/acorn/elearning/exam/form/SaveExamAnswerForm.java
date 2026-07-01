package com.acorn.elearning.exam.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaveExamAnswerForm {
    @NotBlank
    private String answerText;

    private String move = "stay";

    private String idempotencyToken;
}
