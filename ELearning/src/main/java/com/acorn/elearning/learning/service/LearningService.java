package com.acorn.elearning.learning.service;

import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class LearningService {
    public Map<String, Object> stub(String action) {
        // TODO 구현 예시입니다. 실제 parameter와 return DTO로 method signature를 교체하세요.
        // Long userId = sessionUser.userId();
        // List<Subject> subjects = subjectMapper.findAll();
        // List<LearningProgress> progress = learningProgressMapper.findByUserId(userId);
        // return Map.of("dashboard", LearningDashboardResponse.from(subjects, progress));
        return Map.of("action", action, "status", "SKELETON");
    }
}
