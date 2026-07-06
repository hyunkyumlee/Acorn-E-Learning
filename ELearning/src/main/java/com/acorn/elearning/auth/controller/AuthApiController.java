package com.acorn.elearning.auth.controller;

import com.acorn.elearning.auth.dto.request.LoginRequest;
import com.acorn.elearning.auth.dto.request.SignupRequest;
import com.acorn.elearning.auth.dto.response.SocialAccountListResponse;
import com.acorn.elearning.auth.dto.response.SocialAccountResponse;
import com.acorn.elearning.auth.dto.response.UserSessionResponse;
import com.acorn.elearning.auth.service.AuthService;
import com.acorn.elearning.auth.service.OAuthService;
import com.acorn.elearning.auth.service.SessionService;
import com.acorn.elearning.common.response.ApiResponse;
import com.acorn.elearning.common.response.VoidResponse;
import com.acorn.elearning.security.SessionUser;
import java.util.Map;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthApiController {

    private final AuthService authService;
    private final SessionService sessionService;
    private final OAuthService oAuthService;

    public AuthApiController(AuthService authService, SessionService sessionService, OAuthService oAuthService) {
        this.authService = authService;
        this.sessionService = sessionService;
        this.oAuthService = oAuthService;
    }

     @PostMapping("/api/auth/signup")
    public ApiResponse<UserSessionResponse> signup(HttpSession session, @Valid @RequestBody SignupRequest request) {
        UserSessionResponse response = authService.signup(session, request);
        return ApiResponse.success(response);
    }

    @PostMapping("/api/auth/login")
    public ApiResponse<UserSessionResponse> login(HttpSession session, @Valid @RequestBody LoginRequest request) {
        UserSessionResponse response = authService.login(session, request);
        return ApiResponse.success(response);
    }

    @PostMapping("/api/auth/logout")
    public ApiResponse<VoidResponse> logout(HttpSession session) {
        authService.logout(session);
        return ApiResponse.success(VoidResponse.INSTANCE);
    }

    @GetMapping("/api/auth/session")
    public ApiResponse<Object> session(HttpSession session) {
        return sessionService.getUser(session).<ApiResponse<Object>>map(u -> ApiResponse.success(sessionService.toLoginResponse(u)))
                .orElseGet(() -> ApiResponse.success(sessionService.toGuestSessionResponse()));
    }

    @GetMapping("/api/auth/social-accounts")
    public ApiResponse<SocialAccountListResponse> socialAccount(HttpSession session) {
        SessionUser user = sessionService.getRequiredUser(session);
        return ApiResponse.success(oAuthService.socialAccounts(user));
    }

    @DeleteMapping("/api/auth/social-accounts/{provider}")
    public ApiResponse<SocialAccountResponse> deleteSocialAccount(HttpSession session, @PathVariable String provider) {
        SessionUser user = sessionService.getRequiredUser(session);
        return ApiResponse.success(oAuthService.deleteSocialAccount(user, provider));
    }

}
