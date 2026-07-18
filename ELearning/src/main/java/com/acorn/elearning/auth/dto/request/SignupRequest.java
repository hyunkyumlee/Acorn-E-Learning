package com.acorn.elearning.auth.dto.request;

import com.acorn.elearning.common.validation.PasswordPolicy;
import com.acorn.elearning.common.validation.StrongPassword;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record SignupRequest(@NotBlank @Email String email,
                            @NotBlank @StrongPassword String password,
                            @NotBlank String confirmPassword,
                            @NotBlank @Size(min = 2, max = 50) String nickname,
                            Long primarySubjectId, String learningGoal) {

    @AssertTrue(message = "비밀번호에 닉네임이나 이메일 아이디를 포함할 수 없습니다.")
    public boolean isPasswordNotContainingProfile() {
        return !PasswordPolicy.containsProfileInfo(password, nickname, email);
    }

    @AssertTrue(message = "비밀번호 확인이 일치하지 않습니다.")
    public boolean isPasswordConfirmed() {
        if (password == null || confirmPassword == null) return true;
        return password.equals(confirmPassword);
    }
}

//public record SignupRequest(@NotBlank String requestId, Map<String, Object> payload) {}
