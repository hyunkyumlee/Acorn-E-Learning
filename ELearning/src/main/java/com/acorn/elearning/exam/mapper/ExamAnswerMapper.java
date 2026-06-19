package com.acorn.elearning.exam.mapper;

import com.acorn.elearning.exam.model.ExamAnswer;
import java.util.List;
import java.util.Optional;

public interface ExamAnswerMapper {
    Optional<ExamAnswer> findById(Long id);
    List<ExamAnswer> findAll();
    int insert(ExamAnswer model);
    int update(ExamAnswer model);
}
