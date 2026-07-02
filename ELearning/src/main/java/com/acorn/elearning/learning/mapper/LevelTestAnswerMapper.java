package com.acorn.elearning.learning.mapper;

import com.acorn.elearning.learning.model.LevelTestAnswer;
import java.util.List;
import java.util.Optional;

public interface LevelTestAnswerMapper {
    Optional<LevelTestAnswer> findById(Long id);
    List<LevelTestAnswer> findAll();
    /** 결과 화면용: 특정 attempt의 제출 답안 목록을 answer_id 순으로 조회. */
    List<LevelTestAnswer> findByAttemptId(Long attemptId);
    int insert(LevelTestAnswer model);
    int update(LevelTestAnswer model);
}
