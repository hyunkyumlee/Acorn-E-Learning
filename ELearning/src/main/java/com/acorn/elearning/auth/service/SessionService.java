package com.acorn.elearning.auth.service;

import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class SessionService {
    public Map<String, Object> stub(String action) {
        // TODO 구현 예시입니다. 실제 parameter와 return DTO로 method signature를 교체하세요.
        // SessionUser sessionUser = (SessionUser) httpSession.getAttribute("LOGIN_USER");
        // if (sessionUser == null) { return Map.of("authenticated", false); }
        // return Map.of("authenticated", true, "user", sessionUser);
        return Map.of("action", action, "status", "SKELETON");
    }
}
