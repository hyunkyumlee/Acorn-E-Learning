package com.acorn.elearning.learning.service;

import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class LessonService {
    public Map<String, Object> stub(String action) {
        // TODO 구현 예시입니다. 실제 parameter와 return DTO로 method signature를 교체하세요.
        // Lesson lesson = lessonMapper.findById(lessonId).orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND));
        // LearningProgress progress = learningProgressMapper.findByUserIdAndLessonId(userId, lessonId).orElseGet(...);
        // learningProgressMapper.update(progress.complete());
        // return Map.of("lesson", LessonDetailResponse.from(lesson, progress));
        return Map.of("action", action, "status", "SKELETON");
    }
}
