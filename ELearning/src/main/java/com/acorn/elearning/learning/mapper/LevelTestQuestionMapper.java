package com.acorn.elearning.learning.mapper;

import com.acorn.elearning.learning.model.LevelTestQuestion;
import java.util.List;
import java.util.Optional;

public interface LevelTestQuestionMapper {
    Optional<LevelTestQuestion> findById(Long id);
    List<LevelTestQuestion> findAll();
    /** 레벨 테스트 출제용: 특정 과목의 활성(is_active=1) 문항을 question_no 순으로 조회. */
    List<LevelTestQuestion> findActiveBySubjectId(Long subjectId);
    int insert(LevelTestQuestion model);
    int update(LevelTestQuestion model);
}
