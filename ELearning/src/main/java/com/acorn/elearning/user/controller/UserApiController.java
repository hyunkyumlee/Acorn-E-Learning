package com.acorn.elearning.user.controller;

import com.acorn.elearning.common.response.ApiResponse;
import com.acorn.elearning.security.SessionUser;
import com.acorn.elearning.user.dto.request.ChangePasswordRequest;
import com.acorn.elearning.user.dto.request.UpdateProfileRequest;
import com.acorn.elearning.user.dto.request.UpdateSecurityRequest;
import com.acorn.elearning.user.dto.request.UpdateSettingsRequest;
import com.acorn.elearning.user.dto.request.WithdrawUserRequest;
import com.acorn.elearning.user.dto.response.MyPageSummaryResponse;
import com.acorn.elearning.user.dto.response.PaymentHistoryPageResponse;
import com.acorn.elearning.user.dto.response.UserProfileResponse;
import com.acorn.elearning.user.dto.response.UserSettingsResponse;
import com.acorn.elearning.user.service.SettingsService;
import com.acorn.elearning.user.service.UserActivityService;
import com.acorn.elearning.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserApiController {
    private final UserActivityService userActivityService;
    private final UserService userService;
    private final SettingsService settingsService;

    public UserApiController(
            UserActivityService userActivityService,
            UserService userService,
            SettingsService settingsService
    ) {
        this.userActivityService = userActivityService;
        this.userService = userService;
        this.settingsService = settingsService;
    }

    @GetMapping("/api/users/me")
    public ApiResponse<UserProfileResponse> me(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser
    ) {
        return ApiResponse.success(userService.me(sessionUser));
    }

    @PatchMapping("/api/users/me/profile")
    public ApiResponse<UserProfileResponse> updateProfile(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @Valid @RequestBody UpdateProfileRequest request,
            HttpSession httpSession
    ) {
        UserProfileResponse response = userService.updateProfile(sessionUser, request.toForm());
        httpSession.setAttribute(
                SessionUser.SESSION_KEY,
                new SessionUser(response.userId(), response.email(), response.nickname(), response.role(), sessionUser.premiumActive()));
        return ApiResponse.success(response);
    }

    @PatchMapping("/api/users/me/security")
    public ApiResponse<UserProfileResponse> updateSecurity(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @Valid @RequestBody UpdateSecurityRequest request,
            HttpSession httpSession
    ) {
        UserProfileResponse response = settingsService.updateSecurity(sessionUser, request.toForm());
        httpSession.setAttribute(
                SessionUser.SESSION_KEY,
                new SessionUser(response.userId(), response.email(), response.nickname(), response.role(), sessionUser.premiumActive()));
        return ApiResponse.success(response);
    }

    @PatchMapping("/api/users/me/password")
    public ApiResponse<Void> changePassword(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        settingsService.changePassword(sessionUser, request.toForm());
        return ApiResponse.success("비밀번호가 변경되었습니다.", null);
    }

    @GetMapping("/api/users/me/settings")
    public ApiResponse<UserSettingsResponse> settings(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser
    ) {
        return ApiResponse.success(settingsService.settings(sessionUser));
    }

    @PatchMapping("/api/users/me/settings")
    public ApiResponse<UserSettingsResponse> updateSettings(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @Valid @RequestBody UpdateSettingsRequest request
    ) {
        return ApiResponse.success(settingsService.updateSettings(sessionUser, request.toForm()));
    }

    @GetMapping("/api/users/me/payments")
    public ApiResponse<PaymentHistoryPageResponse> payments(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(userActivityService.payments(sessionUser, page, size));
    }

    @GetMapping("/api/mypage/summary")
    public ApiResponse<MyPageSummaryResponse> mypage(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser
    ) {
        return ApiResponse.success(userActivityService.mypage(sessionUser));
    }

    @DeleteMapping("/api/users/me")
    public ApiResponse<UserProfileResponse> withdraw(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @Valid @RequestBody WithdrawUserRequest request,
            HttpSession httpSession
    ) {
        UserProfileResponse response = settingsService.withdraw(sessionUser, request.toForm());
        httpSession.invalidate();
        return ApiResponse.success(response);
    }
}
