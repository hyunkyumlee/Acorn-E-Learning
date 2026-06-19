package com.acorn.elearning.learning.mapper;

import com.acorn.elearning.learning.model.LevelTestAttempt;
import java.util.List;
import java.util.Optional;

public interface LevelTestAttemptMapper {
    Optional<LevelTestAttempt> findById(Long id);
    List<LevelTestAttempt> findAll();
    int insert(LevelTestAttempt model);
    int update(LevelTestAttempt model);
}
