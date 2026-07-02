package com.acorn.elearning.learning.mapper;

import com.acorn.elearning.learning.model.CurriculumNode;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Param;

public interface CurriculumNodeMapper {
    Optional<CurriculumNode> findById(Long id);
    List<CurriculumNode> findAll();
    /**
     * 로드맵 순서(level_code, sort_order, node_id) 기준으로 주어진 노드 "다음" 활성 노드 1건.
     * completeLesson의 nextAction(NEXT_NODE/GATE) 판단용. 다음 노드가 없으면 empty.
     */
    Optional<CurriculumNode> findNextNode(@Param("subjectId") Long subjectId,
                                          @Param("levelCode") String levelCode,
                                          @Param("sortOrder") Integer sortOrder,
                                          @Param("nodeId") Long nodeId);
    int insert(CurriculumNode model);
    int update(CurriculumNode model);
}
