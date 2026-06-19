package com.acorn.elearning.practice.mapper;

import com.acorn.elearning.practice.model.PracticeSetAttempt;
import java.util.List;
import java.util.Optional;

public interface PracticeSetAttemptMapper {
    Optional<PracticeSetAttempt> findById(Long id);
    List<PracticeSetAttempt> findAll();
    int insert(PracticeSetAttempt model);
    int update(PracticeSetAttempt model);
}
