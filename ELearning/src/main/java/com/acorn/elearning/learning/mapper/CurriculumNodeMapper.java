package com.acorn.elearning.learning.mapper;

import com.acorn.elearning.learning.model.CurriculumNode;
import java.util.List;
import java.util.Optional;

public interface CurriculumNodeMapper {
    Optional<CurriculumNode> findById(Long id);
    List<CurriculumNode> findAll();
    int insert(CurriculumNode model);
    int update(CurriculumNode model);
}
