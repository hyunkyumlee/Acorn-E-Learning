package com.acorn.elearning.community.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostSearchCondition {
    @NotBlank
    private String skeletonValue = "TODO";
    private String idempotencyToken;
    private Long id;
}
