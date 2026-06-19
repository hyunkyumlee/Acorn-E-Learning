package com.acorn.elearning.learning.mapper;

import com.acorn.elearning.learning.model.Subject;
import java.util.List;
import java.util.Optional;

public interface SubjectMapper {
    Optional<Subject> findById(Long id);
    List<Subject> findAll();
    int insert(Subject model);
    int update(Subject model);
}
