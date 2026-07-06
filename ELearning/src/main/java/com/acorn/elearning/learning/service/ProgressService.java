package com.acorn.elearning.learning.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.learning.mapper.CurriculumNodeMapper;
import com.acorn.elearning.learning.mapper.LearningProgressMapper;
import com.acorn.elearning.learning.model.CurriculumNode;
import com.acorn.elearning.learning.model.LearningProgress;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProgressService {

    private static final String NODE_TYPE_PLANET = "PLANET";

    /**
     * 단원 진행률 규약(임시): 한쪽만 완료=50, 이론+문제풀이 둘 다=100.
     * LessonService.completeLesson의 규약과 동일하게 맞춘다.
     */
    private static final BigDecimal RATE_PRACTICE_ONLY = new BigDecimal("50.00");
    private static final BigDecimal RATE_FULL = new BigDecimal("100.00");

    private final LearningProgressMapper learningProgressMapper;
    private final CurriculumNodeMapper curriculumNodeMapper;

    public ProgressService(LearningProgressMapper learningProgressMapper,
                           CurriculumNodeMapper curriculumNodeMapper) {
        this.learningProgressMapper = learningProgressMapper;
        this.curriculumNodeMapper = curriculumNodeMapper;
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
     * 로드맵의 완료/현재/잠금 판정을 노드별 learning_progress로 계산한다. (평균 근사치 아님)
     * - completedPlanets: planetNo 오름차순으로 "완료된 행성"이 앞에서부터 몇 개 연속인지.
     *   행성은 순차 학습이라 첫 미완료 행성에서 멈춘다(그 뒤 행성이 완료 상태여도 잠금으로 본다).
     *   완료 기준 = 이론 완료 AND 문제풀이 7/10 통과 (lesson_completed=1 AND practice_passed=1).
     * - progressPercent: 전체 행성 대비 진행률 = 행성별 progress_rate 합 / 전체 행성 수(진행 행이 없는 행성은 0%).
     */
    public RoadmapProgress computeRoadmapProgress(Long userId, Long subjectId, List<CurriculumNode> roadmap) {
        // (user, subject) 진행 행을 nodeId로 매핑 — learning_progress는 (user,subject,node) UNIQUE라 노드당 최대 1행.
        Map<Long, LearningProgress> byNodeId = new HashMap<>();
        for (LearningProgress row : learningProgressMapper.findByUserIdAndSubjectId(userId, subjectId)) {
            byNodeId.put(row.getNodeId(), row);
        }

        List<CurriculumNode> planets = roadmap.stream()
                .filter(node -> NODE_TYPE_PLANET.equals(node.getNodeType()) && node.getPlanetNo() != null)
                .sorted(Comparator.comparingInt(CurriculumNode::getPlanetNo))
                .toList();

        int planetCount = planets.size();
        int completedPlanets = 0;
        boolean contiguous = true;   // 순차 완료가 끊기면(첫 미완료) 이후로는 completed 카운트 중단
        double rateSum = 0d;

        for (CurriculumNode planet : planets) {
            LearningProgress row = byNodeId.get(planet.getNodeId());
            if (row != null && row.getProgressRate() != null) {
                rateSum += row.getProgressRate().doubleValue();
            }
            if (contiguous) {
                if (isCompleted(row)) {
                    completedPlanets++;
                } else {
                    contiguous = false;
                }
            }
        }

        int progressPercent = (planetCount == 0) ? 0 : (int) Math.round(rateSum / planetCount);
        return new RoadmapProgress(completedPlanets, planetCount, progressPercent);
    }

    /** 단원(행성) 완료 = 이론 완료 AND 문제풀이 통과 (10문제 중 7개 이상 정답이면 통과). */
    private boolean isCompleted(LearningProgress row) {
        return row != null
                && Boolean.TRUE.equals(row.getLessonCompleted())
                && Boolean.TRUE.equals(row.getPracticePassed());
    }

    /**
     * 로드맵 진행 요약.
     * @param completedPlanets 앞에서부터 연속 완료된 행성 수(다음 행성 = 현재 학습 대상)
     * @param planetCount      해당 과목 전체 행성 수
     * @param progressPercent  전체 행성 대비 진행률(0~100 정수)
     */
    public record RoadmapProgress(int completedPlanets, int planetCount, int progressPercent) {}
}
