package com.acorn.elearning.admin.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommunityStatusForm {
    @NotBlank
    private String status;
}
