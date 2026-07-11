package com.acorn.elearning.ranking.service;

import java.util.*;


import com.acorn.elearning.ranking.dto.response.MyRankingResponse;
import com.acorn.elearning.ranking.dto.response.RankingPageResponse;
import com.acorn.elearning.ranking.mapper.RankingScoreMapper;

import com.acorn.elearning.ranking.view.RankingPageView;
import com.acorn.elearning.security.SessionUser;
import com.acorn.elearning.user.mapper.UserLearningProfileMapper;
import com.acorn.elearning.user.mapper.UserMapper;

import com.acorn.elearning.user.model.UserLearningProfile;
import org.springframework.stereotype.Service;

@Service
public class RankingService {
    /*
    public Map<String, Object> stub(String action) {
        // TODO 구현 예시입니다. 실제 parameter와 return DTO로 method signature를 교체하세요.
        // List<RankingScore> rows = rankingScoreMapper.findLeaderboard(period, page, size);
        // RankingScore mine = rankingScoreMapper.findMine(userId, period).orElse(null);
        // return Map.of("ranking", RankingPageResponse.from(rows, mine));
        return Map.of("action", action, "status", "SKELETON");
    }
}
*/
        private final RankingScoreMapper rankingScoreMapper;
        private final UserMapper userMapper;
        private final UserLearningProfileMapper userLearningProfileMapper;

        public RankingService(RankingScoreMapper rankingScoreMapper,
                              UserMapper userMapper,
                              UserLearningProfileMapper userLearningProfileMapper) {
            this.rankingScoreMapper = rankingScoreMapper;
            this.userMapper = userMapper;
            this.userLearningProfileMapper = userLearningProfileMapper;
        }

        public RankingPageView index(SessionUser sessionUser, Long subjectId) {
            Map<String, Object> data = buildRankingData(sessionUser, subjectId);

            Map<String, Object> attributes = new HashMap<>();
            attributes.putAll(data);

            return RankingPageView.of("랭킹", attributes);
        }

        public RankingPageResponse rankings(SessionUser sessionUser, Long subjectId) {
            Map<String, Object> data = buildRankingData(sessionUser, subjectId);
            return new RankingPageResponse("SUCCESS", data);
        }

        public MyRankingResponse myRanking(SessionUser sessionUser, Long subjectId) {
            Map<String, Object> data = buildRankingData(sessionUser, subjectId);

            Map<String, Object> myData = new HashMap<>();
            myData.put("selectedSubjectId", data.get("selectedSubjectId"));
            myData.put("periodType", data.get("periodType"));
            myData.put("mySummary", data.get("mySummary"));
            myData.put("scoreBreakdown", data.get("scoreBreakdown"));

            return new MyRankingResponse("SUCCESS", myData);
        }

    private Map<String, Object> buildRankingData(SessionUser sessionUser, Long subjectId) {

            List<Map<String, Object>> filteredScores =
                subjectId == null
                        ? rankingScoreMapper.findWeeklyGlobalRanking()
                        : rankingScoreMapper.findWeeklySubjectRanking(subjectId);


        List<Map<String, Object>> top3 = new ArrayList<>();
        for (int i = 0; i < Math.min(3, filteredScores.size()); i++) {
            Map<String, Object> row = filteredScores.get(i);

            Map<String, Object> item = new HashMap<>();
            item.put("rankNo", i + 1);
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
            item.put("rankNo", i + 1);
            item.put("userId", rowUserId);
            item.put(
                    "nickname",
                    rowUserId != null && rowUserId.equals(sessionUser.userId())
                            ? "나"
                            : row.get("nickname")
            );
            item.put("score", row.get("score"));
            item.put("change", "-");
            rankingList.add(item);
        }

        Map<String, Object> myScoreRow = null;
        int myRankNo = -1;

        for (int i = 0; i < filteredScores.size(); i++) {
            Map<String, Object> row = filteredScores.get(i);
            Long rowUserId = row.get("userId") == null
                    ? null
                    : ((Number) row.get("userId")).longValue();

            if (rowUserId != null && rowUserId.equals(sessionUser.userId())) {
                myScoreRow = row;
                myRankNo = i + 1;
                break;
            }
        }

        Integer gapToPrev = null;
        if (myScoreRow != null && myRankNo > 1) {
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
        mySummary.put("score", myScoreRow != null
                ? ((Number) myScoreRow.get("score")).intValue()
                : 0);
        mySummary.put("gapToPrev", gapToPrev != null ? gapToPrev : "-");

        UserLearningProfile profile = userLearningProfileMapper.findByUserId(sessionUser.userId()).orElse(null);

        Map<String, Object> scoreBreakdown = new HashMap<>();
        scoreBreakdown.put("practiceScore", profile != null && profile.getTotalScore() != null ? profile.getTotalScore() : 0);
        scoreBreakdown.put("examScore", 0);
        scoreBreakdown.put("dailyScore", 0);

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

        Map<String, Object> data = new HashMap<>();
        data.put("subjectTabs", subjectTabs);
        data.put("selectedSubjectId", subjectId);
        data.put("mySummary", mySummary);
        data.put("top3", top3);
        data.put("rankingList", rankingList);
        data.put("scoreBreakdown", scoreBreakdown);
        data.put("periodType", "WEEKLY");

        return data;
    }
    }
