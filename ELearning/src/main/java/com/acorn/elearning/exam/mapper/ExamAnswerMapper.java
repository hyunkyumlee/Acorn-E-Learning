package com.acorn.elearning.exam.mapper;

import com.acorn.elearning.exam.model.ExamAnswer;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Param;

public interface ExamAnswerMapper {
    Optional<ExamAnswer> findById(Long id);
    Optional<ExamAnswer> findByExamIdAndProblemId(@Param("examId") Long examId, @Param("aiProblemId") Long aiProblemId);
    List<ExamAnswer> findByExamId(Long examId);
    List<ExamAnswer> findAll();
    int insert(ExamAnswer model);
    int update(ExamAnswer model);
    int upsertAnswer(ExamAnswer model);
    int updateGradingResult(ExamAnswer model);
}
