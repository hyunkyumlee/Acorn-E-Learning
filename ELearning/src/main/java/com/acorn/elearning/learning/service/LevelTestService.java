package com.acorn.elearning.learning.service;

import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class LevelTestService {
    public Map<String, Object> stub(String action) {
        // TODO 구현 예시입니다. 실제 parameter와 return DTO로 method signature를 교체하세요.
        // SessionUser sessionUser = currentSessionUser();
        // Object entity = domainMapper.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND));
        // domainMapper.update(applyForm(entity, form));
        // return Map.of("result", entity);
        return Map.of("action", action, "status", "SKELETON");
    }
}
