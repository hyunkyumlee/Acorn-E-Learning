package com.acorn.elearning.auth.service;

import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class OAuthService {
    public Map<String, Object> stub(String action) {
        // TODO 구현 예시입니다. 실제 parameter와 return DTO로 method signature를 교체하세요.
        // String state = oAuthStateStore.issue(provider, sessionUser.userId());
        // SocialAccount account = socialAccountMapper.findByProviderAndProviderUserId(provider, providerUserId).orElse(null);
        // socialAccountMapper.insertOrUpdate(account);
        // return Map.of("provider", provider, "connected", true);
        return Map.of("action", action, "status", "SKELETON");
    }
}
