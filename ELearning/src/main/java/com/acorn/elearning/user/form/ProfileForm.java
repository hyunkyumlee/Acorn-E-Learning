package com.acorn.elearning.user.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ProfileForm {
    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(max = 50, message = "닉네임은 50자 이하로 입력해주세요.")
    private String nickname;

    @Size(max = 500, message = "학습 목표는 500자 이하로 입력해주세요.")
    private String learningGoal;

    private Long primarySubjectId;

    private MultipartFile profileImage;

    private boolean resetProfileImage;
}
