package com.acorn.elearning.learning.mapper;

import com.acorn.elearning.learning.model.LevelTestChoice;
import java.util.List;
import java.util.Optional;

public interface LevelTestChoiceMapper {
    Optional<LevelTestChoice> findById(Long id);
    List<LevelTestChoice> findAll();
    /** 문항 화면 표시용: 특정 문항의 선택지를 sort_order 순으로 조회(is_correct 포함, 화면에는 노출 금지). */
    List<LevelTestChoice> findByQuestionId(Long questionId);
    int insert(LevelTestChoice model);
    int update(LevelTestChoice model);
}
