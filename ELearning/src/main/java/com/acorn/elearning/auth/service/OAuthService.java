package com.acorn.elearning.auth.service;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Map;

import com.acorn.elearning.auth.mapper.SocialAccountMapper;
import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.security.SessionUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class OAuthService {

    private static final String OAUTH_STATE_KEY = "OAUTH_STATE";
    private final SocialAccountMapper socialAccountMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    public OAuthService(SocialAccountMapper socialAccountMapper) {
        this.socialAccountMapper = socialAccountMapper;
    }

    public String startLoginRedirect(String provider, HttpSession session) {
        issueState(session, provider);
        return "login?oauth=skeleton&provider=" + provider;
    }

    public String handleLoginCallback(String provider, String state, HttpSession session) {
        validateState(session, provider, state);
        session.removeAttribute(OAUTH_STATE_KEY);
        return "/learning";
    }

    public String startConnectRedirect(String provider, SessionUser sessionUser, HttpSession session) {
        if (sessionUser == null || sessionUser.userId() == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
        issueState(session, provider);
        return "/settings/social?connect=skeleton&provider=" + provider;
    }

    public String handleConnectCallback(String provider, String state, SessionUser sessionUser,HttpSession session) {
        if (sessionUser == null || sessionUser.userId() == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
        validateState(session, provider, state);
        return "/settings/social";
    }

    private void issueState(HttpSession session, String provider) {
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        String state = HexFormat.of().formatHex(bytes);
        session.setAttribute(OAUTH_STATE_KEY, provider + ":" + state);
    }

    private void validateState(HttpSession session, String provider, String state) {
        Object value = session.getAttribute(OAUTH_STATE_KEY);
        if (!(value instanceof String stored) || !stored.equals(provider + ":" + state)) {
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN, "Oauth state 검증에 실패했습니다.");
        }
    }

//    public Map<String, Object> stub(String action) {
//        // TODO 구현 예시입니다. 실제 parameter와 return DTO로 method signature를 교체하세요.
//        // String state = oAuthStateStore.issue(provider, sessionUser.userId());
//        // SocialAccount account = socialAccountMapper.findByProviderAndProviderUserId(provider, providerUserId).orElse(null);
//        // socialAccountMapper.insertOrUpdate(account);
//        // return Map.of("provider", provider, "connected", true);
//        return Map.of("action", action, "status", "SKELETON");
//    }
}
