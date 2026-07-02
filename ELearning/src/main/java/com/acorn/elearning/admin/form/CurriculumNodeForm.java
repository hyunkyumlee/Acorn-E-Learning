package com.acorn.elearning.admin.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CurriculumNodeForm {

    private Long nodeId;
    private Long subjectId;
    private String levelCode;
    private String nodeType;
    private String title;
    private Integer sortOrder;
    private Boolean isActive;
    private String description;

    @NotBlank
    private String skeletonValue = "TODO";
    private String idempotencyToken;
    private Long id;
}
