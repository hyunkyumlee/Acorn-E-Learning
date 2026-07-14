package com.acorn.elearning.auth.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PasswordResetForm {
    @NotBlank private String token;
    @NotBlank @Size(min = 8, max = 72) private String newPassword;
    @NotBlank private String confirmPassword;
}
