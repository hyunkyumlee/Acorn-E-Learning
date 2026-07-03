package com.acorn.elearning.analysis.mapper;

import com.acorn.elearning.analysis.model.AnalysisExamProblemResult;
import com.acorn.elearning.analysis.model.AnalysisExamSummary;
import com.acorn.elearning.analysis.model.AnalysisLearningProgressStat;
import com.acorn.elearning.analysis.model.AnalysisPracticeSummary;
import com.acorn.elearning.analysis.model.AnalysisWrongAnswerNodeStat;
import com.acorn.elearning.analysis.model.AnalysisWrongAnswerSummary;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Param;

public interface AnalysisDashboardMapper {
    Optional<AnalysisExamSummary> findLatestGradedExamSummary(Long userId);

    Optional<AnalysisExamSummary> findExamSummary(
            @Param("userId") Long userId,
            @Param("examId") Long examId
    );

    List<AnalysisExamSummary> findRecentGradedExamSummaries(
            @Param("userId") Long userId,
            @Param("subjectId") Long subjectId,
            @Param("limit") int limit
    );

    List<AnalysisExamProblemResult> findProblemResults(Long examId);

    List<AnalysisLearningProgressStat> findLearningProgressStats(
            @Param("userId") Long userId,
            @Param("subjectId") Long subjectId
    );

    Optional<AnalysisPracticeSummary> findPracticeSummary(
            @Param("userId") Long userId,
            @Param("subjectId") Long subjectId
    );

    Optional<AnalysisWrongAnswerSummary> findWrongAnswerSummary(
            @Param("userId") Long userId,
            @Param("subjectId") Long subjectId
    );

    List<AnalysisWrongAnswerNodeStat> findWrongAnswerNodeStats(
            @Param("userId") Long userId,
            @Param("subjectId") Long subjectId
    );
}
