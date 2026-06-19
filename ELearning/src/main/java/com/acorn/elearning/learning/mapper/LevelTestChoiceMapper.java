package com.acorn.elearning.learning.mapper;

import com.acorn.elearning.learning.model.LevelTestChoice;
import java.util.List;
import java.util.Optional;

public interface LevelTestChoiceMapper {
    Optional<LevelTestChoice> findById(Long id);
    List<LevelTestChoice> findAll();
    int insert(LevelTestChoice model);
    int update(LevelTestChoice model);
}
