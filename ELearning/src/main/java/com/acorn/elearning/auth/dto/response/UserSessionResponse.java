package com.acorn.elearning.auth.dto.response;

import com.acorn.elearning.security.SessionUser;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserSessionResponse(Long userId, String email, String nickname, String role, boolean premiumActive, String redirectUrl) {
    public static UserSessionResponse fromSignup(SessionUser user) {
        return new UserSessionResponse(
                user.userId(), user.email(), user.nickname(), user.role(), user.premiumActive(), user.defaultRedirectPath()
        );
    }

    public static UserSessionResponse fromLogin(SessionUser user) {
        return new UserSessionResponse(
                user.userId(), null, user.nickname(), user.role(), user.premiumActive(), user.defaultRedirectPath()
        );
    }

    public static UserSessionResponse fromLogin(SessionUser user, String redirectUrl) {
        return new UserSessionResponse(
                user.userId(), null, user.nickname(), user.role(), user.premiumActive(), redirectUrl
        );
    }
}


//public record UserSessionResponse(String status, Map<String, Object> data) {
//    public static UserSessionResponse stub() { return new UserSessionResponse("SKELETON", Map.of()); }
//}
