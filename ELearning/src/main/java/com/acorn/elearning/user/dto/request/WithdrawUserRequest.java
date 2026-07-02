package com.acorn.elearning.user.dto.request;

import com.acorn.elearning.user.form.WithdrawUserForm;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record WithdrawUserRequest(
        @NotNull(message = "회원 탈퇴 확인에 동의해주세요.")
        @AssertTrue(message = "회원 탈퇴 확인에 동의해주세요.")
        Boolean confirmed
) {
    public WithdrawUserForm toForm() {
        WithdrawUserForm form = new WithdrawUserForm();
        form.setConfirmed(confirmed);
        return form;
    }
}
