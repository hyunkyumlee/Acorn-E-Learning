package com.acorn.elearning.analysis.mapper;

import com.acorn.elearning.analysis.model.AiAnalysisReport;
import java.util.List;
import java.util.Optional;

public interface AiAnalysisReportMapper {
    Optional<AiAnalysisReport> findById(Long id);
    List<AiAnalysisReport> findAll();
    int insert(AiAnalysisReport model);
    int update(AiAnalysisReport model);
}
