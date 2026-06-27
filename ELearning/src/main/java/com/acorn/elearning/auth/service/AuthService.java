package com.acorn.elearning.auth.service;

import com.acorn.elearning.security.SessionUser;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    public Map<String, Object> stub(String action) {
        // TODO 구현 예시입니다. 실제 parameter와 return DTO로 method signature를 교체하세요.
        // LoginForm form = ...; SignupForm signupForm = ...;
        // UserCredential credential = userCredentialMapper.findByEmail(form.getEmail()).orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED));
        // SessionUser sessionUser = sessionService.createSessionUser(credential.userId());
        // return Map.of("session", sessionUser);
        return Map.of(
                "action", action,
                "status", "SKELETON",
                "redirectUrlByRole", Map.of(
                        SessionUser.ROLE_USER, "/learning",
                        SessionUser.ROLE_ADMIN, "/admin"));
    }
}
