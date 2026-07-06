package com.acorn.elearning.practice.mapper;

import com.acorn.elearning.practice.model.ProblemChoice;
import java.util.List;
import java.util.Optional;

public interface ProblemChoiceMapper {
    Optional<ProblemChoice> findById(Long id);
    List<ProblemChoice> findAll();
    int insert(ProblemChoice model);
    int update(ProblemChoice model);

    // 문제별 보기 조회
    List<ProblemChoice> findByProblemId(Long problemId);
}
