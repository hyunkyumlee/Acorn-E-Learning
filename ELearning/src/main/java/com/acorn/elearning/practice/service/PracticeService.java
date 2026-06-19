package com.acorn.elearning.practice.service;

import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class PracticeService {
    public Map<String, Object> stub(String action) {
        // TODO 구현 예시입니다. 실제 parameter와 return DTO로 method signature를 교체하세요.
        // List<PracticeProblem> problems = practiceProblemMapper.findAvailable(userId, subjectId);
        // PracticeSetAttempt attempt = PracticeSetAttempt.start(userId, problems);
        // practiceSetAttemptMapper.insert(attempt);
        // return Map.of("attempt", PracticeSetResponse.from(attempt, problems));
        return Map.of("action", action, "status", "SKELETON");
    }
}
