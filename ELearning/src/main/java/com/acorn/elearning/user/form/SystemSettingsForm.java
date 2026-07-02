package com.acorn.elearning.user.form;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SystemSettingsForm {
    @Pattern(regexp = "SYSTEM|LIGHT|DARK", message = "테마 값이 올바르지 않습니다.")
    private String theme = "SYSTEM";

    @NotNull(message = "학습 알림 설정을 선택해주세요.")
    private Boolean notificationEnabled = Boolean.TRUE;

    @NotNull(message = "모션 줄이기 설정을 선택해주세요.")
    private Boolean reducedMotionEnabled = Boolean.FALSE;
}
