package com.acorn.elearning.user.dto.response;

import com.acorn.elearning.user.model.UserSetting;
import java.time.LocalDateTime;

public record UserSettingsResponse(
        Long settingId,
        Long userId,
        String theme,
        Boolean notificationEnabled,
        String accessibilityMode,
        Boolean reducedMotionEnabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String themeLabel,
        String notificationLabel,
        String accessibilityModeLabel,
        String reducedMotionLabel
) {
    public static UserSettingsResponse from(Long userId, UserSetting setting) {
        String theme = setting == null || !hasText(setting.getTheme()) ? "SYSTEM" : setting.getTheme();
        Boolean notificationEnabled = setting == null || setting.getNotificationEnabled() == null
                ? Boolean.TRUE
                : setting.getNotificationEnabled();
        Boolean reducedMotionEnabled = setting == null || setting.getReducedMotionEnabled() == null
                ? Boolean.FALSE
                : setting.getReducedMotionEnabled();
        String accessibilityMode = setting == null ? null : setting.getAccessibilityMode();

        return new UserSettingsResponse(
                setting == null ? null : setting.getSettingId(),
                setting == null ? userId : setting.getUserId(),
                theme,
                notificationEnabled,
                accessibilityMode,
                reducedMotionEnabled,
                setting == null ? null : setting.getCreatedAt(),
                setting == null ? null : setting.getUpdatedAt(),
                themeLabel(theme),
                Boolean.TRUE.equals(notificationEnabled) ? "켜짐" : "꺼짐",
                accessibilityModeLabel(accessibilityMode),
                Boolean.TRUE.equals(reducedMotionEnabled) ? "켜짐" : "꺼짐"
        );
    }

    private static String themeLabel(String theme) {
        if ("SYSTEM".equals(theme)) {
            return "시스템 설정 따름";
        }
        if ("LIGHT".equals(theme)) {
            return "라이트";
        }
        if ("DARK".equals(theme)) {
            return "다크";
        }
        return hasText(theme) ? theme : "시스템 설정 따름";
    }

    private static String accessibilityModeLabel(String accessibilityMode) {
        if ("HIGH_CONTRAST".equals(accessibilityMode)) {
            return "고대비";
        }
        if ("DEFAULT".equals(accessibilityMode)) {
            return "기본";
        }
        return hasText(accessibilityMode) ? accessibilityMode : "기본";
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
