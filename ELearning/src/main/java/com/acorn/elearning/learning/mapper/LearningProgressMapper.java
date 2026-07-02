package com.acorn.elearning.learning.mapper;

import com.acorn.elearning.learning.model.LearningProgress;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Param;

public interface LearningProgressMapper {
    Optional<LearningProgress> findById(Long id);
    List<LearningProgress> findAll();
    /** 대시보드 진행률 집계용: 특정 사용자의 과목 진행 행 조회. */
    List<LearningProgress> findByUserIdAndSubjectId(@Param("userId") Long userId, @Param("subjectId") Long subjectId);
    /** completeLesson upsert용: (user, subject, node) UNIQUE 기준 단일 진행 행. 없으면 insert, 있으면 update. */
    Optional<LearningProgress> findByUserSubjectNode(@Param("userId") Long userId,
                                                     @Param("subjectId") Long subjectId,
                                                     @Param("nodeId") Long nodeId);
    int insert(LearningProgress model);
    int update(LearningProgress model);
}
