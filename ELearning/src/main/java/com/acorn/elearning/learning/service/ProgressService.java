package com.acorn.elearning.learning.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.learning.mapper.CurriculumNodeMapper;
import com.acorn.elearning.learning.mapper.LearningProgressMapper;
import com.acorn.elearning.learning.mapper.UserLessonProgressMapper;
import com.acorn.elearning.learning.model.CurriculumNode;
import com.acorn.elearning.learning.model.LearningProgress;
import com.acorn.elearning.learning.view.LevelProgressRow;
import com.acorn.elearning.learning.view.SubjectProgressRow;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProgressService {

    private static final String NODE_TYPE_PLANET = "PLANET";
    /** 레벨 진행 순서(LevelTestService와 같은 규약). 레벨별 진행률을 항상 이 순서로 보여준다. */
    private static final List<String> LEVEL_ORDER = List.of("BRONZE", "SILVER", "GOLD");

    /**
     * 단원 진행률 규약(임시): 한쪽만 완료=50, 이론+문제풀이 둘 다=100.
     * LessonService.completeLesson의 규약과 동일하게 맞춘다.
     */
    private static final BigDecimal RATE_PRACTICE_ONLY = new BigDecimal("50.00");
    private static final BigDecimal RATE_FULL = new BigDecimal("100.00");

    private final LearningProgressMapper learningProgressMapper;
    private final CurriculumNodeMapper curriculumNodeMapper;
    private final CurriculumService curriculumService;
    private final UserLessonProgressMapper userLessonProgressMapper;

    public ProgressService(LearningProgressMapper learningProgressMapper,
                           CurriculumNodeMapper curriculumNodeMapper,
                           CurriculumService curriculumService,
                           UserLessonProgressMapper userLessonProgressMapper) {
        this.learningProgressMapper = learningProgressMapper;
        this.curriculumNodeMapper = curriculumNodeMapper;
        this.curriculumService = curriculumService;
        this.userLessonProgressMapper = userLessonProgressMapper;
    }

    /**
     * 과목별 진행률(0~100)을 한 번의 집계 쿼리로 계산한다. 과목 목록에 진도를 함께 그릴 때 쓴다.
     * 완료 판정은 computeRoadmapProgress와 같은 레슨 단위 규약(이론+문제풀이 둘 다)이지만,
     * 여기서는 행성 단위 평균이 아니라 과목 전체 required 레슨 대비 완료 비율이다.
     * required 레슨이 없는 과목은 0%.
     */
    public Map<Long, Integer> computeSubjectProgress(Long userId) {
        Map<Long, Integer> result = new HashMap<>();
        for (SubjectProgressRow row : userLessonProgressMapper.findLessonStatsBySubject(userId)) {
            int total = row.getTotalLessons();
            int percent = (total == 0) ? 0
                    : (int) Math.round(row.getCompletedLessons() * 100.0 / total);
            result.put(row.getSubjectId(), percent);
        }
        return result;
    }

    /**
     * 한 과목의 레벨별 진행률(0~100)을 레벨 진행 순서대로 계산한다.
     * 계산 규약은 computeSubjectProgress와 같고(해당 범위의 required 레슨 대비 완료 비율) 범위만 레벨로 좁힌다.
     * 레벨 테스트로 상위 레벨에 배정돼도 건너뛴 레벨은 학습한 적이 없으므로 0%로 남는다.
     * 노드가 없는 레벨도 0%로 채워 세 레벨을 항상 같은 순서로 보여준다.
     */
    public Map<String, Integer> computeLevelProgress(Long userId, Long subjectId) {
        Map<String, Integer> percentByLevel = new LinkedHashMap<>();
        for (String levelCode : LEVEL_ORDER) {
            percentByLevel.put(levelCode, 0);
        }
        for (LevelProgressRow row : userLessonProgressMapper.findLessonStatsByLevel(userId, subjectId)) {
            int total = row.getTotalLessons();
            int percent = (total == 0) ? 0
                    : (int) Math.round(row.getCompletedLessons() * 100.0 / total);
            percentByLevel.replace(row.getLevelCode(), percent);
        }
        return percentByLevel;
    }

    /**
     * 문제풀이 세트 통과(10문제 중 7개 이상 정답)를 learning_progress에 기록한다.
     * completeLesson(이론 측)의 대칭 버전 — practice_passed=1을 (user, subject, node) UNIQUE 기준으로 upsert한다.
     * 이론까지 이미 완료된 단원이면 progress_rate=100 + completed_at을 찍어 단원을 완전완료 처리한다.
     *
     * <p>문제풀이 세트 통과 지점(practice 흐름)에서 호출되는 것을 전제로 한다. 호출 측 트랜잭션을
     * 망가뜨리지 않도록:
     *  - 이미 practice_passed 처리된 단원이면 멱등 no-op(중복 호출에도 completed_at을 새로 덮어쓰지 않음).
     *  - 레벨 unlock 검사는 여기서 하지 않는다(문제풀이를 통과했다는 것 자체가 접근 권한을 전제).
     *
     * @throws BusinessException 단원이 없으면 COMMON_NOT_FOUND(404),
     *                           단원이 넘겨받은 과목에 속하지 않으면 COMMON_VALIDATION_FAILED(400).
     */
    @Transactional
    public void markPracticePassed(Long userId, Long subjectId, Long nodeId) {
        CurriculumNode node = curriculumNodeMapper.findById(nodeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "단원 정보를 찾을 수 없습니다."));
        if (!subjectId.equals(node.getSubjectId())) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "단원이 해당 과목에 속하지 않습니다.");
        }

        LearningProgress progress = learningProgressMapper
                .findByUserSubjectNode(userId, subjectId, nodeId)
                .orElse(null);

        // 이미 문제풀이 통과 처리된 단원이면 멱등 종료(중복 통과 호출에도 상태·완료시각 유지).
        if (progress != null && Boolean.TRUE.equals(progress.getPracticePassed())) {
            return;
        }

        boolean lessonCompleted = progress != null && Boolean.TRUE.equals(progress.getLessonCompleted());
        BigDecimal rate = lessonCompleted ? RATE_FULL : RATE_PRACTICE_ONLY;
        LocalDateTime now = LocalDateTime.now();

        if (progress == null) {
            // 진행 행이 없던 단원 → 새 행 insert. 문제풀이만 통과라 단원 완전완료(completed_at)는 아직 아님.
            LearningProgress row = new LearningProgress();
            row.setUserId(userId);
            row.setSubjectId(subjectId);
            row.setNodeId(nodeId);
            row.setLessonCompleted(false);
            row.setPracticePassed(true);
            row.setProgressRate(rate);
            row.setCompletedAt(null);
            learningProgressMapper.insert(row);
        } else {
            // 이론만 되어 있던 단원 → 문제풀이 통과로 갱신. 둘 다 되면 단원 완전완료 시점 기록.
            progress.setPracticePassed(true);
            progress.setProgressRate(rate);
            if (lessonCompleted) {
                progress.setCompletedAt(now);
            }
            learningProgressMapper.update(progress);
        }
    }

    /**
     * 로드맵의 완료/현재/잠금 판정을 행성별 "레슨 단위" 집계로 계산한다.
     * (노드 플래그 learning_progress가 아니라 user_lesson_progress 집계가 source of truth —
     *  노드 플래그는 첫 레슨 완료에도 켜져 행성 조기완료로 잘못 뜨던 문제를 해소한다.)
     * - 행성 완료 = required(활성·필수) 레슨이 1개 이상이고, 그 전부가 theory+practice 완료.
     * - completedPlanets: planetNo 오름차순으로 앞에서부터 연속 완료된 행성 수.
     *   행성은 순차 학습이라 첫 미완료 행성에서 멈춘다(그 뒤 완료여도 잠금으로 본다).
     * - progressPercent: 행성별 (완료 required / 전체 required)의 평균(%). required 0인 행성은 0%.
     */
    public RoadmapProgress computeRoadmapProgress(Long userId, Long subjectId, List<CurriculumNode> roadmap) {
        List<CurriculumNode> planets = roadmap.stream()
                .filter(node -> NODE_TYPE_PLANET.equals(node.getNodeType()) && node.getPlanetNo() != null)
                .sorted(Comparator.comparingInt(CurriculumNode::getPlanetNo))
                .toList();

        int planetCount = planets.size();
        int completedPlanets = 0;
        boolean contiguous = true;   // 순차 완료가 끊기면(첫 미완료) 이후로는 completed 카운트 중단
        double rateSum = 0d;

        for (CurriculumNode planet : planets) {
            Long nodeId = planet.getNodeId();
            int required = curriculumService.countRequiredLessons(nodeId);
            int completed = curriculumService.countCompletedRequiredLessons(userId, nodeId);
            boolean planetDone = required > 0 && completed >= required;

            rateSum += (required > 0) ? (completed * 100.0 / required) : 0d;

            if (contiguous) {
                if (planetDone) {
                    completedPlanets++;
                } else {
                    contiguous = false;
                }
            }
        }

        int progressPercent = (planetCount == 0) ? 0 : (int) Math.round(rateSum / planetCount);
        return new RoadmapProgress(completedPlanets, planetCount, progressPercent);
    }

    /**
     * 로드맵 진행 요약.
     * @param completedPlanets 앞에서부터 연속 완료된 행성 수(다음 행성 = 현재 학습 대상)
     * @param planetCount      해당 과목 전체 행성 수
     * @param progressPercent  전체 행성 대비 진행률(0~100 정수)
     */
    public record RoadmapProgress(int completedPlanets, int planetCount, int progressPercent) {}
}
