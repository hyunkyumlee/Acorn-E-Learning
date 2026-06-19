package com.acorn.elearning.exam.mapper;

import com.acorn.elearning.exam.model.ExamSession;
import java.util.List;
import java.util.Optional;

public interface ExamSessionMapper {
    Optional<ExamSession> findById(Long id);
    List<ExamSession> findAll();
    int insert(ExamSession model);
    int update(ExamSession model);
}
