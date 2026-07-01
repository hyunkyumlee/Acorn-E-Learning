package com.acorn.elearning.exam.mapper;

import com.acorn.elearning.exam.model.ExamSession;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Param;

public interface ExamSessionMapper {
    Optional<ExamSession> findById(Long id);
    Optional<ExamSession> findByIdAndUserId(@Param("examId") Long examId, @Param("userId") Long userId);
    Optional<ExamSession> findLatestByUserId(Long userId);
    List<ExamSession> findByUserId(Long userId);
    List<ExamSession> findAll();
    int insert(ExamSession model);
    int update(ExamSession model);
    int updateStatus(ExamSession model);
    int updateResult(ExamSession model);
}
