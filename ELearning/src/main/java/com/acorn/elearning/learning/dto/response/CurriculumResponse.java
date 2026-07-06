package com.acorn.elearning.learning.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * GET /api/subjects/{subjectId}/curriculum 응답.
 * subject = 과목 요약, levels = 노드에 등장하는 레벨 코드, nodes = 로드맵 노드(+노드별 진행), unlocks = 해금 이력.
 */
public record CurriculumResponse(
        SubjectInfo subject,
        List<String> levels,
        List<Node> nodes,
        List<Unlock> unlocks) {

    public record SubjectInfo(Long subjectId, String subjectCode, String subjectName) {}

    public record Node(
            Long nodeId,
            Long parentNodeId,
            String levelCode,
            String nodeType,
            Integer planetNo,
            String title,
            String description,
            Integer sortOrder,
            String gateCondition,
            NodeProgress progress) {}

    /** 노드 단위 진행 상태. progressRate는 0~100 정수. */
    public record NodeProgress(boolean lessonCompleted, boolean practicePassed, int progressRate) {}

    public record Unlock(String levelCode, String unlockSource, LocalDateTime unlockedAt) {}
}
