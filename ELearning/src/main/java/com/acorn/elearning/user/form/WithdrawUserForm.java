package com.acorn.elearning.user.form;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WithdrawUserForm {
    @NotNull(message = "회원 탈퇴 확인에 동의해주세요.")
    @AssertTrue(message = "회원 탈퇴 확인에 동의해주세요.")
    private Boolean confirmed = Boolean.FALSE;
}
