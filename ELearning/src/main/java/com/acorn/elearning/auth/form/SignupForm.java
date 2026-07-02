package com.acorn.elearning.auth.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupForm {
    @NotBlank @Email private String email;
    @NotBlank @Size(min = 8, max = 72) private String password;
    @NotBlank @Size(min = 2, max = 50) private String nickname;
    private Long primarySubjectId;
    private String learningGoal;

//    private String skeletonValue = "TODO";
//    private String idempotencyToken;
//    private Long id;
}
