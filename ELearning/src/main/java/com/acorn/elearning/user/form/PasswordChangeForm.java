package com.acorn.elearning.user.form;

import com.acorn.elearning.common.validation.StrongPassword;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeForm {
    @NotBlank(message = "현재 비밀번호를 입력해주세요.")
    private String currentPassword;

    @NotBlank(message = "새 비밀번호를 입력해주세요.")
    @StrongPassword(message = "새 비밀번호는 8~16자이며 영문 대소문자, 숫자, 특수문자를 모두 포함해야 합니다.")
    private String newPassword;

    @NotBlank(message = "새 비밀번호 확인을 입력해주세요.")
    private String confirmPassword;

    @AssertTrue(message = "새 비밀번호 확인이 일치하지 않습니다.")
    public boolean isPasswordConfirmed() {
        if (newPassword == null || confirmPassword == null) {
            return true;
        }
        return newPassword.equals(confirmPassword);
    }
}
