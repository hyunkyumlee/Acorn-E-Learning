package com.acorn.elearning.auth.form;

import com.acorn.elearning.common.validation.StrongPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PasswordResetForm {
    @NotBlank private String token;
    @NotBlank @StrongPassword private String newPassword;
    @NotBlank private String confirmPassword;
}
