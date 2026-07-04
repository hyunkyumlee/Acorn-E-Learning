package com.acorn.elearning.learning.service;

import com.acorn.elearning.learning.mapper.LearningProgressMapper;
import com.acorn.elearning.learning.model.CurriculumNode;
import com.acorn.elearning.learning.model.LearningProgress;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ProgressService {

    private static final String NODE_TYPE_PLANET = "PLANET";

    private final LearningProgressMapper learningProgressMapper;

    public ProgressService(LearningProgressMapper learningProgressMapper) {
        this.learningProgressMapper = learningProgressMapper;
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
