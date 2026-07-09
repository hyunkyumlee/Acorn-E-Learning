package com.acorn.elearning.admin.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubjectForm {

    private Long subjectId;

    private String subjectName;
    private Boolean isActive;
    private String description;


}
