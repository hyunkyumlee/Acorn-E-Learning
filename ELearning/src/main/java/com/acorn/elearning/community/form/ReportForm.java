package com.acorn.elearning.community.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportForm {
    @NotBlank
    @Size(max = 30)
    private String targetType;

    @NotNull
    private Long targetId;

    @NotBlank
    @Size(max = 30)
    private String reasonCode;

    private String idempotencyToken;
}
