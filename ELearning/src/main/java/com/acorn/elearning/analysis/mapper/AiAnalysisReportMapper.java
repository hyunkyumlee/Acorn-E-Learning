package com.acorn.elearning.analysis.mapper;

import com.acorn.elearning.analysis.model.AiAnalysisReport;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Param;

public interface AiAnalysisReportMapper {
    Optional<AiAnalysisReport> findById(Long id);
    Optional<AiAnalysisReport> findByIdAndUserId(@Param("reportId") Long reportId, @Param("userId") Long userId);
    Optional<AiAnalysisReport> findByExamId(Long examId);
    Optional<AiAnalysisReport> findByExamIdAndUserId(@Param("examId") Long examId, @Param("userId") Long userId);
    Optional<AiAnalysisReport> findByExamIdAndUserIdForUpdate(
            @Param("examId") Long examId,
            @Param("userId") Long userId);
    List<AiAnalysisReport> findByUserId(Long userId);
    List<AiAnalysisReport> findAll();
    int insert(AiAnalysisReport model);
    int claimRetry(
            @Param("reportId") Long reportId,
            @Param("userId") Long userId,
            @Param("retryCount") int retryCount);
    int update(AiAnalysisReport model);
}
