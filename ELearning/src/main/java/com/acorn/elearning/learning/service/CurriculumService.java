package com.acorn.elearning.learning.service;

import com.acorn.elearning.learning.mapper.CurriculumNodeMapper;
import com.acorn.elearning.learning.mapper.LessonMapper;
import com.acorn.elearning.learning.model.CurriculumNode;
import com.acorn.elearning.learning.model.Lesson;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CurriculumService {

    private final CurriculumNodeMapper curriculumNodeMapper;
    private final LessonMapper lessonMapper;

    public CurriculumService(CurriculumNodeMapper curriculumNodeMapper, LessonMapper lessonMapper) {
        this.curriculumNodeMapper = curriculumNodeMapper;
        this.lessonMapper = lessonMapper;
    }

    /**
     * 특정 과목의 활성 커리큘럼 노드(로드맵)를 조회한다.
     * CurriculumNodeMapper.findAll()이 subject_id, level_code, sort_order 순으로 정렬해 주므로
     * 여기서는 과목/활성 필터만 적용한다.
     */
    public List<CurriculumNode> getRoadmap(Long subjectId) {
        return curriculumNodeMapper.findAll().stream()
                .filter(node -> subjectId.equals(node.getSubjectId()))
                .filter(node -> Boolean.TRUE.equals(node.getIsActive()))
                .toList();
    }

    /** 단일 lesson 상세 조회. 없으면 null을 반환한다. */
    public Lesson getLessonDetail(Long lessonId) {
        return lessonMapper.findById(lessonId).orElse(null);
    }

    /** 로드맵 각 노드의 활성 레슨 수(nodeId → count). hover 카드 "N개 레슨" 메타용. */
    public Map<Long, Integer> getLessonCountsByNodes(List<CurriculumNode> roadmap) {
        Set<Long> nodeIds = roadmap.stream()
                .map(CurriculumNode::getNodeId)
                .collect(Collectors.toSet());
        Map<Long, Integer> counts = new HashMap<>();
        for (Lesson lesson : lessonMapper.findAll()) {
            if (Boolean.TRUE.equals(lesson.getIsActive())
                    && nodeIds.contains(lesson.getNodeId())) {
                counts.merge(lesson.getNodeId(), 1, Integer::sum);
            }
        }
        return counts;
    }

    public Map<String, Object> stub(String action) {
        // TODO 구현 예시입니다. 실제 parameter와 return DTO로 method signature를 교체하세요.
        // SessionUser sessionUser = currentSessionUser();
        // Object entity = domainMapper.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND));
        // domainMapper.update(applyForm(entity, form));
        // return Map.of("result", entity);
        return Map.of("action", action, "status", "SKELETON");
    }
}
