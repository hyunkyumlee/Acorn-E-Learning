package com.acorn.elearning.learning.mapper;

import com.acorn.elearning.learning.model.LevelTestAnswer;
import java.util.List;
import java.util.Optional;

public interface LevelTestAnswerMapper {
    Optional<LevelTestAnswer> findById(Long id);
    List<LevelTestAnswer> findAll();
    int insert(LevelTestAnswer model);
    int update(LevelTestAnswer model);
}
