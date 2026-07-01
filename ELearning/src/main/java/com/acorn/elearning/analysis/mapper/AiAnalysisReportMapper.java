package com.acorn.elearning.analysis.mapper;

import com.acorn.elearning.analysis.model.AiAnalysisReport;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Param;

public interface AiAnalysisReportMapper {
    Optional<AiAnalysisReport> findById(Long id);
    Optional<AiAnalysisReport> findByIdAndUserId(@Param("reportId") Long reportId, @Param("userId") Long userId);
    Optional<AiAnalysisReport> findByExamId(Long examId);
    List<AiAnalysisReport> findByUserId(Long userId);
    List<AiAnalysisReport> findAll();
    int insert(AiAnalysisReport model);
    int update(AiAnalysisReport model);
}
