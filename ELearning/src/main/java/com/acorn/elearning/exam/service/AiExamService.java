package com.acorn.elearning.exam.service;

import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AiExamService {
    public Map<String, Object> stub(String action) {
        // TODO 구현 예시입니다. 실제 parameter와 return DTO로 method signature를 교체하세요.
        // PremiumGrant grant = premiumGrantMapper.findActiveByUserId(userId).orElseThrow(() -> new BusinessException(ErrorCode.AUTH_FORBIDDEN));
        // ExamSession session = ExamSession.start(userId, grant.expiresAt());
        // examSessionMapper.insert(session);
        // return Map.of("exam", ExamSessionResponse.from(session));
        return Map.of("action", action, "status", "SKELETON");
    }
}
