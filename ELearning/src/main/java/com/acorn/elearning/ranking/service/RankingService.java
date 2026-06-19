package com.acorn.elearning.ranking.service;

import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class RankingService {
    public Map<String, Object> stub(String action) {
        // TODO 구현 예시입니다. 실제 parameter와 return DTO로 method signature를 교체하세요.
        // List<RankingScore> rows = rankingScoreMapper.findLeaderboard(period, page, size);
        // RankingScore mine = rankingScoreMapper.findMine(userId, period).orElse(null);
        // return Map.of("ranking", RankingPageResponse.from(rows, mine));
        return Map.of("action", action, "status", "SKELETON");
    }
}
