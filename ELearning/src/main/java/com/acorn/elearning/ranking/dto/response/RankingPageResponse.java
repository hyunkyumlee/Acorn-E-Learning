package com.acorn.elearning.ranking.dto.response;

import java.util.Map;

public record RankingPageResponse(String status, Map<String, Object> data) {
    public static RankingPageResponse stub() { return new RankingPageResponse("SKELETON", Map.of()); }
}
