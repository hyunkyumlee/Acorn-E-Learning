package com.acorn.elearning.learning.view;

/**
 * completeLesson 결과 View (SR-005 / LEARN-005).
 * REST ProgressUpdateResponse{nodeId, lessonCompleted, progressRate, nextAction}와 대응하는 화면/redirect용 값.
 *
 * @param nodeId         완료 처리된 단원(node) id
 * @param lessonCompleted 이론 완료 여부(항상 true로 반환 — 완료 처리 결과)
 * @param progressRate   해당 단원 진행률(0~100 정수). 이론만 완료=50, 문제풀이까지=100 (임시 규약)
 * @param nextAction     다음 행동 힌트: START_PRACTICE(문제풀이 남음) / NEXT_NODE(다음 단원) / GATE(레벨 내 단원 모두 완료)
 * @param nextNodeId     nextAction=NEXT_NODE일 때 다음 단원 id, 그 외 null
 */
public record LessonProgressView(
        Long nodeId,
        boolean lessonCompleted,
        int progressRate,
        String nextAction,
        Long nextNodeId
) {
}
