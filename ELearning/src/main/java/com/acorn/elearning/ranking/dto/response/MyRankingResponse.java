package com.acorn.elearning.ranking.dto.response;

import java.util.Map;

public record MyRankingResponse(String status, Map<String, Object> data) {
    public static MyRankingResponse stub() { return new MyRankingResponse("SKELETON", Map.of()); }
}
