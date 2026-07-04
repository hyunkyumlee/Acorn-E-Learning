package com.acorn.elearning.admin.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportHandleForm {
    @NotBlank
    private String status;
    private String memo;
}
