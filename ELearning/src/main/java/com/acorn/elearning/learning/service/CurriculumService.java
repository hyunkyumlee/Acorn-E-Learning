package com.acorn.elearning.learning.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.learning.dto.response.CurriculumResponse;
import com.acorn.elearning.learning.mapper.CurriculumNodeMapper;
import com.acorn.elearning.learning.mapper.LearningProgressMapper;
import com.acorn.elearning.learning.mapper.LessonMapper;
import com.acorn.elearning.learning.mapper.SubjectMapper;
import com.acorn.elearning.learning.mapper.UserLevelUnlockMapper;
import com.acorn.elearning.learning.model.CurriculumNode;
import com.acorn.elearning.learning.model.LearningProgress;
import com.acorn.elearning.learning.model.Lesson;
import com.acorn.elearning.learning.model.Subject;
import com.acorn.elearning.learning.model.UserLevelUnlock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CurriculumService {

    private final CurriculumNodeMapper curriculumNodeMapper;
    private final LessonMapper lessonMapper;
    private final SubjectMapper subjectMapper;
    private final LearningProgressMapper learningProgressMapper;
    private final UserLevelUnlockMapper userLevelUnlockMapper;

    public CurriculumService(CurriculumNodeMapper curriculumNodeMapper,
                             LessonMapper lessonMapper,
                             SubjectMapper subjectMapper,
                             LearningProgressMapper learningProgressMapper,
                             UserLevelUnlockMapper userLevelUnlockMapper) {
        this.curriculumNodeMapper = curriculumNodeMapper;
        this.lessonMapper = lessonMapper;
        this.subjectMapper = subjectMapper;
        this.learningProgressMapper = learningProgressMapper;
        this.userLevelUnlockMapper = userLevelUnlockMapper;
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

    /**
     * 특정 과목·레벨의 활성 커리큘럼 노드(로드맵 한 판)를 조회한다.
     * 레벨별로 판을 나눠 표시하기 위해 subject_id + level_code로 DB에서 직접 필터한다.
     */
    public List<CurriculumNode> getRoadmap(Long subjectId, String levelCode) {
        return curriculumNodeMapper.findBySubjectAndLevel(subjectId, levelCode);
    }

    /** 특정 과목에서 사용자가 해금한 레벨 코드 집합. 로드맵 레벨 탭의 활성/잠금 판정용. */
    public Set<String> getUnlockedLevelCodes(Long userId, Long subjectId) {
        return userLevelUnlockMapper.findByUserAndSubject(userId, subjectId).stream()
                .map(UserLevelUnlock::getLevelCode)
                .collect(Collectors.toSet());
    }

    /** 단일 lesson 상세 조회. 없으면 null을 반환한다. */
    public Lesson getLessonDetail(Long lessonId) {
        return lessonMapper.findById(lessonId).orElse(null);
    }

    /** 특정 사용자의 해당 노드 이론(lesson) 완료 여부. 이론학습 화면의 완료 상태 표시용. */
    public boolean isLessonTheoryCompleted(Long userId, Long nodeId) {
        CurriculumNode node = curriculumNodeMapper.findById(nodeId).orElse(null);
        if (node == null) {
            return false;
        }
        return learningProgressMapper.findByUserSubjectNode(userId, node.getSubjectId(), nodeId)
                .map(p -> Boolean.TRUE.equals(p.getLessonCompleted()))
                .orElse(false);
    }

    /**
     * 커리큘럼 조회(REST): 과목 요약 + 레벨 목록 + 노드(+노드별 진행) + 해금 이력.
     * levelCode가 있으면 해당 레벨 노드만 필터한다. 과목 없으면 404.
     */
    @Transactional(readOnly = true)
    public CurriculumResponse getCurriculumResponse(Long userId, Long subjectId, String levelCode) {
        Subject subject = subjectMapper.findById(subjectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "과목을 찾을 수 없습니다."));

        List<CurriculumNode> roadmap = getRoadmap(subjectId).stream()
                .filter(node -> levelCode == null || levelCode.equals(node.getLevelCode()))
                .toList();

        Map<Long, LearningProgress> progressByNode = new HashMap<>();
        for (LearningProgress row : learningProgressMapper.findByUserIdAndSubjectId(userId, subjectId)) {
            progressByNode.put(row.getNodeId(), row);
        }

        List<CurriculumResponse.Node> nodes = roadmap.stream()
                .map(node -> {
                    LearningProgress row = progressByNode.get(node.getNodeId());
                    CurriculumResponse.NodeProgress nodeProgress = new CurriculumResponse.NodeProgress(
                            row != null && Boolean.TRUE.equals(row.getLessonCompleted()),
                            row != null && Boolean.TRUE.equals(row.getPracticePassed()),
                            (row != null && row.getProgressRate() != null) ? row.getProgressRate().intValue() : 0);
                    return new CurriculumResponse.Node(
                            node.getNodeId(), node.getParentNodeId(), node.getLevelCode(), node.getNodeType(),
                            node.getPlanetNo(), node.getTitle(), node.getDescription(), node.getSortOrder(),
                            node.getGateCondition(), nodeProgress);
                })
                .toList();

        List<String> levels = roadmap.stream()
                .map(CurriculumNode::getLevelCode)
                .distinct()
                .toList();

        List<CurriculumResponse.Unlock> unlocks = userLevelUnlockMapper.findByUserAndSubject(userId, subjectId).stream()
                .map(u -> new CurriculumResponse.Unlock(u.getLevelCode(), u.getUnlockSource(), u.getUnlockedAt()))
                .toList();

        return new CurriculumResponse(
                new CurriculumResponse.SubjectInfo(
                        subject.getSubjectId(), subject.getSubjectCode(), subject.getSubjectName()),
                levels, nodes, unlocks);
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
}
