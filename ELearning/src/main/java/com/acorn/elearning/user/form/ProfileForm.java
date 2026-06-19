package com.acorn.elearning.user.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileForm {
    @NotBlank
    private String skeletonValue = "TODO";
    private String idempotencyToken;
    private Long id;
}
