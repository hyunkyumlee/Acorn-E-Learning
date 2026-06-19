package com.acorn.elearning.exam.mapper;

import com.acorn.elearning.exam.model.AiExamProblem;
import java.util.List;
import java.util.Optional;

public interface AiExamProblemMapper {
    Optional<AiExamProblem> findById(Long id);
    List<AiExamProblem> findAll();
    int insert(AiExamProblem model);
    int update(AiExamProblem model);
}
