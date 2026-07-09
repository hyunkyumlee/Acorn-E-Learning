package com.acorn.elearning.practice.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePracticeSetForm {
    @NotBlank
    private String skeletonValue = "TODO";
    private String idempotencyToken;
    private Long id;

    @NotNull
    private Long subjectId;

    @NotNull
    private Long nodeId;

    @NotNull
    private Long lessonId;

    @NotBlank
    private String difficultyCode;


}
