package com.acorn.elearning.ranking.mapper;

import com.acorn.elearning.ranking.model.RankingScore;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface RankingScoreMapper {
    Optional<RankingScore> findById(Long id);

    List<RankingScore> findAll();

    int insert(RankingScore model);

    int update(RankingScore model);

    int deleteWeeklySubjectRankingByPeriodKey(@Param("periodKey") String periodKey);

    int insertWeeklySubjectRankingScores(@Param("periodKey") String periodKey);

    int deleteMonthlyGlobalRankingByPeriodKey(@Param("periodKey") String periodKey);

    int insertMonthlyGlobalRankingScores(@Param("periodKey") String periodKey);

    int deleteMonthlySubjectRankingByPeriodKey(@Param("periodKey") String periodKey);

    int insertMonthlySubjectRankingScores(@Param("periodKey") String periodKey);


    List<Map<String, Object>> findWeeklySubjectRanking(
            @Param("subjectId") Long subjectId,
            @Param("periodKey") String periodKey
    );

    Map<String, Object> findMyWeeklySubjectRanking(
            @Param("userId") Long userId,
            @Param("subjectId") Long subjectId,
            @Param("periodKey") String periodKey
    );

    List<Map<String, Object>> findMonthlyGlobalRankingFromSubjects(
            @Param("periodKey") String periodKey
    );

    Map<String, Object> findMyMonthlyGlobalRankingFromSubjects(
            @Param("userId") Long userId,
            @Param("periodKey") String periodKey
    );

    Integer sumWeeklyPracticeScore(
            @Param("userId") Long userId,
            @Param("subjectId") Long subjectId
    );

    Integer sumWeeklyExamScore(
            @Param("userId") Long userId,
            @Param("subjectId") Long subjectId
    );

    Integer sumWeeklyDailyScore(
            @Param("userId") Long userId,
            @Param("subjectId") Long subjectId
    );

    Integer sumMonthlyPracticeScore(
            @Param("userId") Long userId,
            @Param("subjectId") Long subjectId
    );

    Integer sumMonthlyExamScore(
            @Param("userId") Long userId,
            @Param("subjectId") Long subjectId
    );

    Integer sumMonthlyDailyScore(
            @Param("userId") Long userId,
            @Param("subjectId") Long subjectId
    );
}
