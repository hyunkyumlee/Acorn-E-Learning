package com.acorn.elearning.auth.controller;

import com.acorn.elearning.common.response.ApiResponse;
import com.acorn.elearning.security.SessionUser;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthApiController {

    @PostMapping("/api/auth/signup")
    public ApiResponse<Map<String, Object>> signup() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // SignupForm form = request body 또는 form binding 값으로 받으세요.
        // UserSessionResponse response = authService.signup(sessionUser, form);
        // return ApiResponse.success(response);
        return ok("AUTH-001");
    }

    @PostMapping("/api/auth/login")
    public ApiResponse<Map<String, Object>> login() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // LoginForm form = request body 또는 form binding 값으로 받으세요.
        // UserSessionResponse response = authService.login(sessionUser, form);
        // return ApiResponse.success(response);
        return ApiResponse.success(Map.of(
                "endpointId", "AUTH-002",
                "status", "SKELETON",
                "redirectUrlByRole", Map.of(
                        SessionUser.ROLE_USER, "/learning",
                        SessionUser.ROLE_ADMIN, "/admin")));
    }

    @PostMapping("/api/auth/logout")
    public ApiResponse<Map<String, Object>> logout() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // LogoutForm form = request body 또는 form binding 값으로 받으세요.
        // GuestSessionResponse response = sessionService.logout(sessionUser, form);
        // return ApiResponse.success(response);
        return ok("AUTH-003");
    }

    @GetMapping("/api/auth/session")
    public ApiResponse<Map<String, Object>> session() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // UserSessionResponse response = sessionService.session();
        // return ApiResponse.success(response);
        return ok("AUTH-004");
    }

    @GetMapping("/api/auth/social-accounts")
    public ApiResponse<Map<String, Object>> socialAccounts() {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // SocialAccountListResponse response = oAuthService.socialAccounts(sessionUser);
        // return ApiResponse.success(response);
        return ok("AUTH-007");
    }

    @DeleteMapping("/api/auth/social-accounts/{provider}")
    public ApiResponse<Map<String, Object>> deleteSocialAccount(@PathVariable String provider) {
        // TODO 구현 예시입니다. 실제 signature에 필요한 @Validated Form, BindingResult, SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // SocialDisconnectForm form = request body 또는 form binding 값으로 받으세요.
        // SocialAccountResponse response = oAuthService.deleteSocialAccount(sessionUser, form, provider);
        // return ApiResponse.success(response);
        return ok("AUTH-008");
    }

    private ApiResponse<Map<String, Object>> ok(String endpointId) {
        // TODO: 개별 endpoint method에서 service 호출과 Response DTO 변환을 끝내면 이 helper를 제거하세요.
        // return ApiResponse.success(response); 형태가 최종 구현입니다.
        return ApiResponse.success(Map.of("endpointId", endpointId, "status", "SKELETON"));
    }
}
