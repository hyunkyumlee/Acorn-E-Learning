package com.acorn.elearning.user.dto.request;

import com.acorn.elearning.user.form.ProfileForm;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank(message = "닉네임을 입력해주세요.")
        @Size(max = 50, message = "닉네임은 50자 이하로 입력해주세요.")
        String nickname,

        @Size(max = 500, message = "학습 목표는 500자 이하로 입력해주세요.")
        String learningGoal
) {
    public ProfileForm toForm() {
        ProfileForm form = new ProfileForm();
        form.setNickname(nickname);
        form.setLearningGoal(learningGoal);
        return form;
    }
}
