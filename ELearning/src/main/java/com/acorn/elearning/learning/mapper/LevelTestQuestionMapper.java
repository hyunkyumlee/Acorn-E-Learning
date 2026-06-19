package com.acorn.elearning.learning.mapper;

import com.acorn.elearning.learning.model.LevelTestQuestion;
import java.util.List;
import java.util.Optional;

public interface LevelTestQuestionMapper {
    Optional<LevelTestQuestion> findById(Long id);
    List<LevelTestQuestion> findAll();
    int insert(LevelTestQuestion model);
    int update(LevelTestQuestion model);
}
