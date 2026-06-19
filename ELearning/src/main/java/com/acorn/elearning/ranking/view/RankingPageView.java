package com.acorn.elearning.ranking.view;

import java.util.Map;

public record RankingPageView(String title, String status, Map<String, Object> attributes) {
    public static RankingPageView stub(String title) { return new RankingPageView(title, "SKELETON", Map.of()); }
}
