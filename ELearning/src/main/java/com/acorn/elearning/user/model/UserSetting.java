    package com.acorn.elearning.user.model;

    import java.time.LocalDateTime;
    import lombok.Getter;
    import lombok.Setter;

    @Getter
    @Setter
    public class UserSetting {
        private Long settingId;
private Long userId;
private String theme;
private Boolean notificationEnabled;
private String accessibilityMode;
private Boolean reducedMotionEnabled;
private LocalDateTime createdAt;
private LocalDateTime updatedAt;
    }
