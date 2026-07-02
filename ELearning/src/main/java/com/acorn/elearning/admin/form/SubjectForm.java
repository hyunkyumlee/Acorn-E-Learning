package com.acorn.elearning.admin.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubjectForm {

    //등록할 때는 Null
    private Long subjectId;

    private String subjectName;
    private Boolean isActive;
    private String description;

    @NotBlank
    private String skeletonValue = "TODO";
    private String idempotencyToken;
    private Long id;
}
