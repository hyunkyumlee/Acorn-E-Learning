package com.acorn.elearning.user.dto.request;

import com.acorn.elearning.common.validation.StrongPassword;
import com.acorn.elearning.user.form.PasswordChangeForm;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;

public record ChangePasswordRequest(
        @NotBlank(message = "현재 비밀번호를 입력해주세요.")
        String currentPassword,

        @NotBlank(message = "새 비밀번호를 입력해주세요.")
        @StrongPassword(message = "새 비밀번호는 8~16자이며 영문 대소문자, 숫자, 특수문자를 모두 포함해야 합니다.")
        String newPassword,

        @NotBlank(message = "새 비밀번호 확인을 입력해주세요.")
        String confirmPassword
) {
    public PasswordChangeForm toForm() {
        PasswordChangeForm form = new PasswordChangeForm();
        form.setCurrentPassword(currentPassword);
        form.setNewPassword(newPassword);
        form.setConfirmPassword(confirmPassword);
        return form;
    }

    @AssertTrue(message = "새 비밀번호 확인이 일치하지 않습니다.")
    public boolean isPasswordConfirmed() {
        if (newPassword == null || confirmPassword == null) {
            return true;
        }
        return newPassword.equals(confirmPassword);
    }
}
