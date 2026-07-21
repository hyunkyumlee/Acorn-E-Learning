package com.acorn.elearning.ranking.service;

import com.acorn.elearning.ranking.dto.response.MyRankingResponse;
import com.acorn.elearning.ranking.dto.response.RankingPageResponse;
import com.acorn.elearning.ranking.mapper.RankingScoreMapper;
import com.acorn.elearning.ranking.view.RankingPageView;
import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.security.SessionUser;
import com.acorn.elearning.learning.mapper.LearningProfileReadMapper;
import com.acorn.elearning.user.model.UserLearningProfile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RankingService {

    private static final String PERIOD_WEEKLY = "WEEKLY";
    private static final String PERIOD_MONTHLY = "MONTHLY";

    private final RankingScoreMapper rankingScoreMapper;
    private final LearningProfileReadMapper learningProfileReadMapper;

    public RankingService(RankingScoreMapper rankingScoreMapper, LearningProfileReadMapper learningProfileReadMapper) {
        this.rankingScoreMapper = rankingScoreMapper;
        this.learningProfileReadMapper = learningProfileReadMapper;
    }

    @Transactional
    public RankingPageView index(SessionUser sessionUser, Long subjectId, String periodType) {
        Map<String, Object> data = buildRankingData(sessionUser, subjectId);

        Map<String, Object> attributes = new HashMap<>();
        attributes.putAll(data);

        return RankingPageView.of("랭킹", attributes);
    }

    @Transactional
    public RankingPageResponse rankings(SessionUser sessionUser, Long subjectId, String periodType) {
        Map<String, Object> data = buildRankingData(sessionUser, subjectId);
        return new RankingPageResponse("SUCCESS", data);
    }

    @Transactional
    public MyRankingResponse myRanking(SessionUser sessionUser, Long subjectId, String periodType) {
        Map<String, Object> data = buildRankingData(sessionUser, subjectId);

        Map<String, Object> myData = new HashMap<>();
        myData.put("selectedSubjectId", data.get("selectedSubjectId"));
        myData.put("periodType", data.get("periodType"));
        myData.put("mySummary", data.get("mySummary"));
        myData.put("scoreBreakdown", data.get("scoreBreakdown"));

        return new MyRankingResponse("SUCCESS", myData);
    }

    private Map<String, Object> buildRankingData(SessionUser sessionUser, Long subjectId) {
        String effectivePeriodType = resolvePeriodType(subjectId);

        String leagueCode = resolveCurrentLeagueCode(sessionUser.userId());

        List<Map<String, Object>> filteredScores =
                subjectId == null
                        ? rankingScoreMapper.findMonthlyGlobalRankingFromSubjects(
                        currentMonthlyPeriodKey(),
                        leagueCode
                )
                        : rankingScoreMapper.findWeeklySubjectRanking(
                        subjectId,
                        currentWeeklyPeriodKey(),
                        leagueCode
                );

        List<Map<String, Object>> top3 = new ArrayList<>();
        for (int i = 0; i < Math.min(3, filteredScores.size()); i++) {
            Map<String, Object> row = filteredScores.get(i);

            Map<String, Object> item = new HashMap<>();
            item.put("rankNo", row.get("rankNo"));
            item.put("userId", row.get("userId"));
            item.put("nickname", row.get("nickname"));
            item.put("score", row.get("score"));
            top3.add(item);
        }

        List<Map<String, Object>> rankingList = new ArrayList<>();
        for (int i = 3; i < filteredScores.size(); i++) {
            Map<String, Object> row = filteredScores.get(i);

            Long rowUserId = row.get("userId") == null
                    ? null
                    : ((Number) row.get("userId")).longValue();

            Map<String, Object> item = new HashMap<>();
            item.put("rankNo", row.get("rankNo"));
            item.put("userId", rowUserId);
            item.put("nickname",
                    rowUserId != null && rowUserId.equals(sessionUser.userId())
                            ? "나"
                            : row.get("nickname"));
            item.put("score", row.get("score"));
            item.put("change", "-");
            rankingList.add(item);
        }

        Map<String, Object> myScoreRow =
                subjectId == null
                        ? rankingScoreMapper.findMyMonthlyGlobalRankingFromSubjects(
                        sessionUser.userId(),
                        currentMonthlyPeriodKey(),
                        leagueCode
                )
                        : rankingScoreMapper.findMyWeeklySubjectRanking(
                        sessionUser.userId(),
                        subjectId,
                        currentWeeklyPeriodKey(),
                        leagueCode
                );

        int myRankNo = -1;
        if (myScoreRow != null && myScoreRow.get("rankNo") != null) {
            myRankNo = ((Number) myScoreRow.get("rankNo")).intValue();
        }

        Integer gapToPrev = null;
        if (myScoreRow != null && myRankNo > 1 && filteredScores.size() >= myRankNo - 1) {
            Map<String, Object> prevRow = filteredScores.get(myRankNo - 2);

            Integer prevScore = prevRow.get("score") == null
                    ? 0
                    : ((Number) prevRow.get("score")).intValue();

            Integer myScore = myScoreRow.get("score") == null
                    ? 0
                    : ((Number) myScoreRow.get("score")).intValue();

            gapToPrev = prevScore - myScore;
        }

        Map<String, Object> mySummary = new HashMap<>();
        mySummary.put("nickname", "나");
        mySummary.put("rankNo", myScoreRow != null ? myRankNo : "-");
        mySummary.put("score",
                myScoreRow != null && myScoreRow.get("score") != null
                        ? ((Number) myScoreRow.get("score")).intValue()
                        : 0);
        mySummary.put("gapToPrev", gapToPrev != null ? gapToPrev : "-");

        Map<String, Object> scoreBreakdown = buildScoreBreakdown(
                sessionUser.userId(),
                subjectId,
                effectivePeriodType
        );

        List<Map<String, Object>> subjectTabs = new ArrayList<>();

        Map<String, Object> tab0 = new HashMap<>();
        tab0.put("label", "통합");
        tab0.put("subjectId", "");
        subjectTabs.add(tab0);

        Map<String, Object> tab1 = new HashMap<>();
        tab1.put("label", "JAVA");
        tab1.put("subjectId", 1L);
        subjectTabs.add(tab1);

        Map<String, Object> tab2 = new HashMap<>();
        tab2.put("label", "SQL");
        tab2.put("subjectId", 2L);
        subjectTabs.add(tab2);

        Map<String, Object> tab3 = new HashMap<>();
        tab3.put("label", "Python");
        tab3.put("subjectId", 3L);
        subjectTabs.add(tab3);

        Map<String, Object> tab4 = new HashMap<>();
        tab4.put("label", "HTML/CSS");
        tab4.put("subjectId", 4L);
        subjectTabs.add(tab4);

        List<Map<String, Object>> leagueTabs = new ArrayList<>();

        Map<String, Object> league1 = new HashMap<>();
        league1.put("code", "BRONZE");
        league1.put("label", "BRONZE");
        league1.put("active", "BRONZE".equals(leagueCode));
        leagueTabs.add(league1);

        Map<String, Object> league2 = new HashMap<>();
        league2.put("code", "SILVER");
        league2.put("label", "SILVER");
        league2.put("active", "SILVER".equals(leagueCode));
        leagueTabs.add(league2);

        Map<String, Object> league3 = new HashMap<>();
        league3.put("code", "GOLD");
        league3.put("label", "GOLD");
        league3.put("active", "GOLD".equals(leagueCode));
        leagueTabs.add(league3);

        Map<String, Object> data = new HashMap<>();
        data.put("subjectTabs", subjectTabs);
        data.put("selectedSubjectId", subjectId);
        data.put("mySummary", mySummary);
        data.put("top3", top3);
        data.put("rankingList", rankingList);
        data.put("scoreBreakdown", scoreBreakdown);
        data.put("periodType", effectivePeriodType);
        data.put("currentLeagueCode", leagueCode);
        data.put("leagueTabs", leagueTabs);

        return data;
    }

    private Map<String, Object> buildScoreBreakdown(Long userId, Long subjectId, String periodType) {
        Map<String, Object> scoreBreakdown = new HashMap<>();

        if (PERIOD_WEEKLY.equals(periodType)) {
            scoreBreakdown.put(
                    "practiceScore",
                    safeScore(rankingScoreMapper.sumWeeklyPracticeScore(userId, subjectId))
            );
            scoreBreakdown.put(
                    "examScore",
                    safeScore(rankingScoreMapper.sumWeeklyExamScore(userId, subjectId))
            );
            scoreBreakdown.put(
                    "dailyScore",
                    safeScore(rankingScoreMapper.sumWeeklyDailyScore(userId, subjectId))
            );
        } else {
            scoreBreakdown.put(
                    "practiceScore",
                    safeScore(rankingScoreMapper.sumMonthlyPracticeScore(userId, subjectId))
            );
            scoreBreakdown.put(
                    "examScore",
                    safeScore(rankingScoreMapper.sumMonthlyExamScore(userId, subjectId))
            );
            scoreBreakdown.put(
                    "dailyScore",
                    safeScore(rankingScoreMapper.sumMonthlyDailyScore(userId, subjectId))
            );
        }

        return scoreBreakdown;
    }


    @Transactional
    public void refreshRankingScores(Long subjectId, String periodType) {
        String periodKey = PERIOD_WEEKLY.equals(periodType)
                ? currentWeeklyPeriodKey()
                : currentMonthlyPeriodKey();
        String lockName = "ranking-refresh:" + periodType + ":" + periodKey;
        Integer lockAcquired = rankingScoreMapper.tryAcquireRefreshLock(lockName, 10);
        if (!Integer.valueOf(1).equals(lockAcquired)) {
            throw new BusinessException(
                    ErrorCode.COMMON_IDEMPOTENCY_CONFLICT,
                    "같은 기간의 랭킹을 재계산 중입니다. 잠시 후 다시 시도해 주세요."
            );
        }

        try {
            if (PERIOD_WEEKLY.equals(periodType)) {
                rankingScoreMapper.deleteWeeklySubjectRankingByPeriodKey(periodKey);
                rankingScoreMapper.insertWeeklySubjectRankingScores(periodKey);
                return;
            }

            if (PERIOD_MONTHLY.equals(periodType)) {
                rankingScoreMapper.deleteMonthlySubjectRankingByPeriodKey(periodKey);
                rankingScoreMapper.insertMonthlySubjectRankingScores(periodKey);

                rankingScoreMapper.deleteMonthlyGlobalRankingByPeriodKey(periodKey);
                rankingScoreMapper.insertMonthlyGlobalRankingScores(periodKey);
            }
        } finally {
            rankingScoreMapper.releaseRefreshLock(lockName);
        }
    }

    private String resolvePeriodType(Long subjectId) {
        return subjectId == null ? PERIOD_MONTHLY : PERIOD_WEEKLY;
    }

    private String currentWeeklyPeriodKey() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        WeekFields weekFields = WeekFields.ISO;

        int weekBasedYear = today.get(weekFields.weekBasedYear());
        int week = today.get(weekFields.weekOfWeekBasedYear());

        return String.format("%d-W%02d", weekBasedYear, week);
    }

    private String currentMonthlyPeriodKey() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        return String.format("%d-%02d", today.getYear(), today.getMonthValue());
    }

    private int safeScore(Integer score) {
        return score == null ? 0 : score;
    }


private String resolveCurrentLeagueCode(Long userId) {
    return learningProfileReadMapper.findByUserId(userId)
            .map(UserLearningProfile::getCurrentLevelCode)
            .filter(levelCode -> levelCode != null && !levelCode.isBlank())
            .orElse("BRONZE");
}
}
