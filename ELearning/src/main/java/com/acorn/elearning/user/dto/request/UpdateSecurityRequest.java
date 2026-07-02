package com.acorn.elearning.user.dto.request;

import com.acorn.elearning.user.form.SecurityForm;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateSecurityRequest(
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @Size(max = 255, message = "이메일은 255자 이하로 입력해주세요.")
        String email
) {
    public SecurityForm toForm() {
        SecurityForm form = new SecurityForm();
        form.setEmail(email);
        return form;
    }
}
