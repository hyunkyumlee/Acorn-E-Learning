package com.acorn.elearning.learning.service;

import com.acorn.elearning.learning.mapper.SubjectMapper;
import com.acorn.elearning.learning.model.Subject;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class LearningService {

    private final SubjectMapper subjectMapper;

    public LearningService(SubjectMapper subjectMapper) {
        this.subjectMapper = subjectMapper;
    }

    /**
     * 학습 메인 화면용: 활성(is_active=1) 과목 목록을 sort_order 순으로 조회한다.
     * SubjectMapper.findAll()이 sort_order ASC로 정렬해 주므로 여기서는 활성 필터만 적용한다.
     */
    public List<Subject> getActiveSubjects() {
        return subjectMapper.findAll().stream()
                .filter(subject -> Boolean.TRUE.equals(subject.getIsActive()))
                .toList();
    }

    public Map<String, Object> stub(String action) {
        // TODO 구현 예시입니다. 실제 parameter와 return DTO로 method signature를 교체하세요.
        // Long userId = sessionUser.userId();
        // List<Subject> subjects = subjectMapper.findAll();
        // List<LearningProgress> progress = learningProgressMapper.findByUserId(userId);
        // return Map.of("dashboard", LearningDashboardResponse.from(subjects, progress));
        return Map.of("action", action, "status", "SKELETON");
    }
}
