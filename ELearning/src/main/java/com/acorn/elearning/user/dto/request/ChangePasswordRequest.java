package com.acorn.elearning.user.dto.request;

import com.acorn.elearning.user.form.PasswordChangeForm;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "현재 비밀번호를 입력해주세요.")
        String currentPassword,

        @NotBlank(message = "새 비밀번호를 입력해주세요.")
        @Size(min = 8, max = 72, message = "새 비밀번호는 8자 이상 72자 이하로 입력해주세요.")
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
