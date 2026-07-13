package com.acorn.elearning.exam.mapper;

import com.acorn.elearning.exam.model.AiExamProblem;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Param;

public interface AiExamProblemMapper {
    Optional<AiExamProblem> findById(Long id);
    Optional<AiExamProblem> findByIdAndExamId(@Param("aiProblemId") Long aiProblemId, @Param("examId") Long examId);
    List<AiExamProblem> findByExamId(Long examId);
    List<AiExamProblem> findByExamIdForUpdate(Long examId);
    List<AiExamProblem> findAll();
    int insert(AiExamProblem model);
    int update(AiExamProblem model);
}
