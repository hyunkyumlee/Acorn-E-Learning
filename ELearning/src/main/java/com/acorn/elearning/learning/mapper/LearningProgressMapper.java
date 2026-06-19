package com.acorn.elearning.learning.mapper;

import com.acorn.elearning.learning.model.LearningProgress;
import java.util.List;
import java.util.Optional;

public interface LearningProgressMapper {
    Optional<LearningProgress> findById(Long id);
    List<LearningProgress> findAll();
    int insert(LearningProgress model);
    int update(LearningProgress model);
}
