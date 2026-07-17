package com.acorn.elearning.learning.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.learning.dto.response.CurriculumResponse;
import com.acorn.elearning.learning.mapper.CurriculumNodeMapper;
import com.acorn.elearning.learning.mapper.LearningProgressMapper;
import com.acorn.elearning.learning.mapper.LessonMapper;
import com.acorn.elearning.learning.mapper.SubjectMapper;
import com.acorn.elearning.learning.mapper.UserLessonProgressMapper;
import com.acorn.elearning.learning.mapper.UserLevelUnlockMapper;
import com.acorn.elearning.learning.model.CurriculumNode;
import com.acorn.elearning.learning.model.LearningProgress;
import com.acorn.elearning.learning.model.Lesson;
import com.acorn.elearning.learning.model.Subject;
import com.acorn.elearning.learning.model.UserLessonProgress;
import com.acorn.elearning.learning.model.UserLevelUnlock;
import com.acorn.elearning.learning.view.SubjectLevelSummary;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CurriculumService {

    /** 과목 소개 화면에서 요약할 레벨 순서. */
    private static final List<String> LEVEL_ORDER = List.of("BRONZE", "SILVER", "GOLD");
    /** 시험 관문 노드. 행성 수에서 제외한다. */
    private static final String NODE_TYPE_GATE = "GATE";

    private final CurriculumNodeMapper curriculumNodeMapper;
    private final LessonMapper lessonMapper;
    private final SubjectMapper subjectMapper;
    private final LearningProgressMapper learningProgressMapper;
    private final UserLevelUnlockMapper userLevelUnlockMapper;
    private final UserLessonProgressMapper userLessonProgressMapper;

    public CurriculumService(CurriculumNodeMapper curriculumNodeMapper,
                             LessonMapper lessonMapper,
                             SubjectMapper subjectMapper,
                             LearningProgressMapper learningProgressMapper,
                             UserLevelUnlockMapper userLevelUnlockMapper,
                             UserLessonProgressMapper userLessonProgressMapper) {
        this.curriculumNodeMapper = curriculumNodeMapper;
        this.lessonMapper = lessonMapper;
        this.subjectMapper = subjectMapper;
        this.learningProgressMapper = learningProgressMapper;
        this.userLevelUnlockMapper = userLevelUnlockMapper;
        this.userLessonProgressMapper = userLessonProgressMapper;
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

    /**
     * 특정 과목의 해금 기록 전체(레벨 코드 + 해금 사유).
     * 한 화면에서 해금 레벨과 해금 사유를 둘 다 봐야 할 때 조회를 두 번 하지 않으려고 행을 그대로 돌려준다.
     */
    public List<UserLevelUnlock> getUnlocks(Long userId, Long subjectId) {
        return userLevelUnlockMapper.findByUserAndSubject(userId, subjectId);
    }

    /** 해금 기록에서 레벨 코드만. */
    public static Set<String> levelCodesOf(List<UserLevelUnlock> unlocks) {
        return unlocks.stream()
                .map(UserLevelUnlock::getLevelCode)
                .collect(Collectors.toSet());
    }

    /**
     * 해금 기록 중 관문(AI 코딩테스트)을 통과해 열린 레벨.
     * 수강 등록이나 레벨 테스트 배정으로 열린 레벨은 스스로 관문을 넘은 것이 아니므로 제외한다.
     */
    public static Set<String> examUnlockedLevelCodesOf(List<UserLevelUnlock> unlocks) {
        return unlocks.stream()
                .filter(unlock -> UnlockService.SOURCE_AI_EXAM_PASS.equals(unlock.getUnlockSource()))
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

    /**
     * 과목 소개 화면: 레벨별로 행성 몇 개, 레슨 몇 개를 배우는지 요약한다.
     * GATE 노드는 학습 단원이 아니므로 행성 수에서 제외한다.
     */
    public List<SubjectLevelSummary> getLevelSummaries(Long subjectId) {
        List<SubjectLevelSummary> summaries = new ArrayList<>();
        for (String levelCode : LEVEL_ORDER) {
            List<CurriculumNode> nodes = getRoadmap(subjectId, levelCode);
            Map<Long, Integer> lessonCounts = getLessonCountsByNodes(nodes);
            int planetCount = 0;
            int lessonCount = 0;
            for (CurriculumNode node : nodes) {
                if (NODE_TYPE_GATE.equals(node.getNodeType())) {
                    continue;
                }
                planetCount++;
                lessonCount += lessonCounts.getOrDefault(node.getNodeId(), 0);
            }
            summaries.add(new SubjectLevelSummary(levelCode, planetCount, lessonCount));
        }
        return summaries;
    }

    /** 단일 노드(행성) 상세. 없으면 null. */
    public CurriculumNode getNodeDetail(Long nodeId) {
        return curriculumNodeMapper.findById(nodeId).orElse(null);
    }

    /** 특정 노드(행성)의 활성 레슨 목록(sort_order 오름차순). 레슨 선택 화면용. */
    public List<Lesson> getLessonsByNode(Long nodeId) {
        return lessonMapper.findAll().stream()
                .filter(lesson -> nodeId.equals(lesson.getNodeId()))
                .filter(lesson -> Boolean.TRUE.equals(lesson.getIsActive()))
                .sorted(java.util.Comparator.comparing(Lesson::getSortOrder,
                        java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())))
                .toList();
    }

    /** 레슨 선택 화면: 노드의 레슨별 진행상태 맵(lessonId → progress). 없는 레슨은 미기록(진행 0). */
    public Map<Long, UserLessonProgress> getLessonProgressMap(Long userId, Long nodeId) {
        Map<Long, UserLessonProgress> map = new HashMap<>();
        for (UserLessonProgress row : userLessonProgressMapper.findByUserAndNode(userId, nodeId)) {
            map.put(row.getLessonId(), row);
        }
        return map;
    }

    /** 노드의 완료 계산 대상(활성·required) 레슨 수. */
    public int countRequiredLessons(Long nodeId) {
        return (int) getLessonsByNode(nodeId).stream()
                .filter(l -> Boolean.TRUE.equals(l.getRequiredForCompletion()))
                .count();
    }

    /** 노드에서 완료(theory+practice)한 required 레슨 수. */
    public int countCompletedRequiredLessons(Long userId, Long nodeId) {
        return userLessonProgressMapper.countCompletedRequiredLessons(userId, nodeId);
    }

    /** 노드에서 문제 풀이(practice)를 통과한 required 레슨 수. */
    public int countPracticePassedRequiredLessons(Long userId, Long nodeId) {
        return userLessonProgressMapper.countPracticePassedRequiredLessons(userId, nodeId);
    }

    /** 특정 레슨의 이론 완료 여부(레슨 단위). 이론 학습 화면 완료 표시용. */
    public boolean isTheoryCompletedForLesson(Long userId, Long lessonId) {
        return userLessonProgressMapper.findByUserAndLesson(userId, lessonId)
                .map(p -> Boolean.TRUE.equals(p.getTheoryCompleted()))
                .orElse(false);
    }
}
