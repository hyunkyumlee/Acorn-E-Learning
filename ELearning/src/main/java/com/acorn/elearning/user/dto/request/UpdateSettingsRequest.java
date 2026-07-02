package com.acorn.elearning.user.dto.request;

import com.acorn.elearning.user.form.SystemSettingsForm;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UpdateSettingsRequest(
        @Pattern(regexp = "SYSTEM|LIGHT|DARK", message = "테마 값이 올바르지 않습니다.")
        String theme,

        @NotNull(message = "학습 알림 설정을 선택해주세요.")
        Boolean notificationEnabled,

        @NotNull(message = "모션 줄이기 설정을 선택해주세요.")
        Boolean reducedMotionEnabled
) {
    public SystemSettingsForm toForm() {
        SystemSettingsForm form = new SystemSettingsForm();
        form.setTheme(theme);
        form.setNotificationEnabled(notificationEnabled);
        form.setReducedMotionEnabled(reducedMotionEnabled);
        return form;
    }
}
