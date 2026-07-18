package com.acorn.elearning.auth.form;

import com.acorn.elearning.common.validation.PasswordPolicy;
import com.acorn.elearning.common.validation.StrongPassword;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupForm {
    @NotBlank @Email private String email;
    @NotBlank @StrongPassword private String password;
    @NotBlank private String confirmPassword;
    @NotBlank @Size(min = 2, max = 50) private String nickname;
    private Long primarySubjectId;
    private String learningGoal;

    @AssertTrue(message = "비밀번호에 닉네임이나 이메일 아이디를 포함할 수 없습니다.")
    public boolean isPasswordNotContainingProfile() {
        return !PasswordPolicy.containsProfileInfo(password, nickname, email);
    }

    @AssertTrue(message = "비밀번호 확인이 일치하지 않습니다.")
    public boolean isPasswordConfirmed() {
        if (password == null || confirmPassword == null) return true;
        return password.equals(confirmPassword);
    }

//    private String skeletonValue = "TODO";
//    private String idempotencyToken;
//    private Long id;
}
