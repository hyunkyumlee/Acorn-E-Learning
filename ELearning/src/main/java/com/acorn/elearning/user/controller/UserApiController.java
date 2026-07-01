package com.acorn.elearning.user.controller;

import com.acorn.elearning.common.response.ApiResponse;
import com.acorn.elearning.security.SessionUser;
import com.acorn.elearning.user.dto.response.PaymentHistoryPageResponse;
import com.acorn.elearning.user.service.UserActivityService;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserApiController {
    private final UserActivityService userActivityService;

    public UserApiController(UserActivityService userActivityService) {
        this.userActivityService = userActivityService;
    }

    @GetMapping("/api/users/me")
    public ApiResponse<Map<String, Object>> me() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // UserProfileResponse response = userService.me(sessionUser);
        // return ApiResponse.success(response);
        return ok("USER-001");
    }

    @PatchMapping("/api/users/me/profile")
    public ApiResponse<Map<String, Object>> updateProfile() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // ProfileForm form = request body 또는 form binding 값으로 받으세요.
        // UserProfileResponse response = userService.updateProfile(sessionUser, form);
        // return ApiResponse.success(response);
        return ok("USER-002");
    }

    @PatchMapping("/api/users/me/security")
    public ApiResponse<Map<String, Object>> updateSecurity() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // SecurityForm form = request body 또는 form binding 값으로 받으세요.
        // UserSettingsResponse response = settingsService.updateSecurity(sessionUser, form);
        // return ApiResponse.success(response);
        return ok("USER-003");
    }

    @GetMapping("/api/users/me/settings")
    public ApiResponse<Map<String, Object>> settings() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // UserSettingsResponse response = settingsService.settings(sessionUser);
        // return ApiResponse.success(response);
        return ok("USER-004");
    }

    @PatchMapping("/api/users/me/settings")
    public ApiResponse<Map<String, Object>> updateSettings() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // SystemSettingsForm form = request body 또는 form binding 값으로 받으세요.
        // UserSettingsResponse response = settingsService.updateSettings(sessionUser, form);
        // return ApiResponse.success(response);
        return ok("USER-004");
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
    public ApiResponse<Map<String, Object>> mypage() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // MyPageSummaryResponse response = userActivityService.mypage(sessionUser);
        // return ApiResponse.success(response);
        return ok("USER-006");
    }

    @DeleteMapping("/api/users/me")
    public ApiResponse<Map<String, Object>> withdraw() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // WithdrawUserForm form = request body 또는 form binding 값으로 받으세요.
        // UserSettingsResponse response = userService.withdraw(sessionUser, form);
        // return ApiResponse.success(response);
        return ok("USER-007");
    }

    private ApiResponse<Map<String, Object>> ok(String endpointId) {
        // TODO: 개별 endpoint method에서 service 호출과 Response DTO 변환을 끝내면 이 helper를 제거하세요.
        // return ApiResponse.success(response); 형태가 최종 구현입니다.
        return ApiResponse.success(Map.of("endpointId", endpointId, "status", "SKELETON"));
    }
}
