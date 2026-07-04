package com.acorn.elearning.analysis.mapper;

import com.acorn.elearning.analysis.model.AnalysisCodingAnswerSummary;
import com.acorn.elearning.analysis.model.AnalysisCodingExamAggregate;
import com.acorn.elearning.analysis.model.AnalysisCodingMistakeStat;
import com.acorn.elearning.analysis.model.AnalysisExamSummary;
import com.acorn.elearning.analysis.model.AnalysisLevelSummary;
import com.acorn.elearning.analysis.model.AnalysisLearningProgressStat;
import com.acorn.elearning.analysis.model.AnalysisPracticeSummary;
import com.acorn.elearning.analysis.model.AnalysisSubjectSummary;
import com.acorn.elearning.analysis.model.AnalysisWrongAnswerNodeStat;
import com.acorn.elearning.analysis.model.AnalysisWrongAnswerSummary;
import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Param;

public interface AnalysisDashboardMapper {
    List<AnalysisSubjectSummary> findSubjectSummaries(@Param("userId") Long userId);

    List<AnalysisLevelSummary> findLevelSummaries(@Param("userId") Long userId);

    List<AnalysisCodingAnswerSummary> findCodingAnswerSummaries(@Param("userId") Long userId);

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

    List<AnalysisExamSummary> findRecentGradedExamSummariesByUser(
            @Param("userId") Long userId,
            @Param("limit") int limit
    );

    List<AnalysisExamSummary> findAllGradedExamSummariesByUser(@Param("userId") Long userId);

    List<AnalysisCodingMistakeStat> findCodingMistakeStats(
            @Param("userId") Long userId,
            @Param("subjectId") Long subjectId
    );

    List<AnalysisCodingMistakeStat> findCodingMistakeStatsByUser(@Param("userId") Long userId);

    Optional<AnalysisCodingExamAggregate> findCodingExamAggregate(
            @Param("userId") Long userId,
            @Param("subjectId") Long subjectId
    );

    Optional<AnalysisCodingExamAggregate> findCodingExamAggregateByUser(@Param("userId") Long userId);

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
