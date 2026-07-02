package com.acorn.elearning.user.dto.request;

import com.acorn.elearning.user.form.SystemSettingsForm;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UpdateSettingsRequest(
        @Pattern(regexp = "SYSTEM|LIGHT|DARK", message = "테마 값이 올바르지 않습니다.")
        String theme,

        Boolean darkModeEnabled,

        @NotNull(message = "학습 알림 설정을 선택해주세요.")
        Boolean notificationEnabled,

        @NotNull(message = "이메일 알림 설정을 선택해주세요.")
        Boolean reducedMotionEnabled,

        @Pattern(regexp = "KO|EN|JA|ZH", message = "표시 언어 값이 올바르지 않습니다.")
        String displayLanguage
) {
    public SystemSettingsForm toForm() {
        SystemSettingsForm form = new SystemSettingsForm();
        form.setTheme(theme);
        form.setDarkModeEnabled(darkModeEnabled != null ? darkModeEnabled : "DARK".equals(theme));
        form.setNotificationEnabled(notificationEnabled);
        form.setReducedMotionEnabled(reducedMotionEnabled);
        form.setDisplayLanguage(displayLanguage);
        return form;
    }
}
