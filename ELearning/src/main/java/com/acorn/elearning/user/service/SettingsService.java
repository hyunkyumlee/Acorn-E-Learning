package com.acorn.elearning.user.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.security.SessionUser;
import com.acorn.elearning.user.dto.response.UserProfileResponse;
import com.acorn.elearning.user.dto.response.UserSettingsResponse;
import com.acorn.elearning.user.form.PasswordChangeForm;
import com.acorn.elearning.user.form.ProfileForm;
import com.acorn.elearning.user.form.SecurityForm;
import com.acorn.elearning.user.form.SystemSettingsForm;
import com.acorn.elearning.user.form.WithdrawUserForm;
import com.acorn.elearning.user.mapper.UserSettingMapper;
import com.acorn.elearning.user.model.UserSetting;
import com.acorn.elearning.user.view.SettingsHomeView;
import com.acorn.elearning.user.view.SocialAccountView;
import com.acorn.elearning.user.view.WithdrawConfirmView;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettingsService {
    private final UserService userService;
    private final UserSettingMapper userSettingMapper;
    private final ObjectProvider<PasswordChangePort> passwordChangePort;

    public SettingsService(
            UserService userService,
            UserSettingMapper userSettingMapper,
            ObjectProvider<PasswordChangePort> passwordChangePort
    ) {
        this.userService = userService;
        this.userSettingMapper = userSettingMapper;
        this.passwordChangePort = passwordChangePort;
    }

    @Transactional(readOnly = true)
    public SettingsHomeView index(SessionUser sessionUser) {
        UserProfileResponse profile = profile(sessionUser);
        UserSettingsResponse settings = settings(sessionUser);
        return new SettingsHomeView("설정", profile, settings);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse profile(SessionUser sessionUser) {
        return userService.me(sessionUser);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse security(SessionUser sessionUser) {
        return userService.me(sessionUser);
    }

    @Transactional
    public UserProfileResponse updateProfile(SessionUser sessionUser, ProfileForm form) {
        return userService.updateProfile(sessionUser, form);
    }

    @Transactional
    public UserProfileResponse updateSecurity(SessionUser sessionUser, SecurityForm form) {
        return userService.updateSecurity(sessionUser, form);
    }

    @Transactional
    public void changePassword(SessionUser sessionUser, PasswordChangeForm form) {
        Long userId = userService.requireUserId(sessionUser);
        PasswordChangePort port = passwordChangePort.getIfAvailable();
        if (port == null) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "비밀번호 변경 기능은 auth 연결 후 사용할 수 있습니다.");
        }
        port.changePassword(userId, form.getCurrentPassword(), form.getNewPassword());
    }

    @Transactional(readOnly = true)
    public SocialAccountView social(SessionUser sessionUser) {
        return new SocialAccountView("연동된 소셜 계정", userService.me(sessionUser));
    }

    @Transactional(readOnly = true)
    public UserSettingsResponse settings(SessionUser sessionUser) {
        Long userId = userService.requireUserId(sessionUser);
        UserSetting setting = userSettingMapper.findByUserId(userId).orElse(null);
        return UserSettingsResponse.from(userId, setting);
    }

    @Transactional(readOnly = true)
    public UserSettingsResponse system(SessionUser sessionUser) {
        return settings(sessionUser);
    }

    @Transactional
    public UserSettingsResponse updateSettings(SessionUser sessionUser, SystemSettingsForm form) {
        Long userId = userService.requireUserId(sessionUser);
        UserSetting setting = userSettingMapper.findByUserId(userId).orElse(null);

        if (setting == null) {
            setting = new UserSetting();
            setting.setUserId(userId);
            setting.setAccessibilityMode(null);
            applySettings(setting, form);
            userSettingMapper.insert(setting);
        } else {
            applySettings(setting, form);
            userSettingMapper.update(setting);
        }

        return UserSettingsResponse.from(userId, setting);
    }

    @Transactional(readOnly = true)
    public WithdrawConfirmView withdrawConfirm(SessionUser sessionUser) {
        return new WithdrawConfirmView("회원 탈퇴", userService.me(sessionUser));
    }

    @Transactional
    public UserProfileResponse withdraw(SessionUser sessionUser, WithdrawUserForm form) {
        return userService.withdraw(sessionUser, form);
    }

    private void applySettings(UserSetting setting, SystemSettingsForm form) {
        setting.setTheme(Boolean.TRUE.equals(form.getDarkModeEnabled()) ? "DARK" : "LIGHT");
        setting.setNotificationEnabled(Boolean.TRUE.equals(form.getNotificationEnabled()));
        setting.setAccessibilityMode(normalizeDisplayLanguage(form.getDisplayLanguage()));
        setting.setReducedMotionEnabled(Boolean.TRUE.equals(form.getReducedMotionEnabled()));
    }

    private String normalizeTheme(String theme) {
        if (theme == null || theme.isBlank()) {
            return "SYSTEM";
        }
        return theme.trim();
    }

    private String normalizeDisplayLanguage(String displayLanguage) {
        if (displayLanguage == null || displayLanguage.isBlank()) {
            return "KO";
        }
        return displayLanguage.trim();
    }
}
