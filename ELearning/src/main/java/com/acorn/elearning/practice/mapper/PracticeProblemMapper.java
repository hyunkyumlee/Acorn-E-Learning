package com.acorn.elearning.practice.mapper;

import com.acorn.elearning.practice.model.PracticeProblem;
import java.util.List;
import java.util.Optional;

public interface PracticeProblemMapper {
    Optional<PracticeProblem> findById(Long id);
    List<PracticeProblem> findAll();
    int insert(PracticeProblem model);
    int update(PracticeProblem model);
}
