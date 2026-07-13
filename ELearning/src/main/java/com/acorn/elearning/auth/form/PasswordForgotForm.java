package com.acorn.elearning.auth.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordForgotForm {
    @NotBlank @Email private String email;
}
