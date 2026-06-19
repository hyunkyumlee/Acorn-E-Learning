package com.acorn.elearning.user.service;

import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {
    public Map<String, Object> stub(String action) {
        // TODO 구현 예시입니다. 실제 parameter와 return DTO로 method signature를 교체하세요.
        // Long userId = sessionUser.userId();
        // User user = userMapper.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND));
        // userMapper.update(applyForm(user, form));
        // return Map.of("user", UserProfileResponse.from(user));
        return Map.of("action", action, "status", "SKELETON");
    }
}
