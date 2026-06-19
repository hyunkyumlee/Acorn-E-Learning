package com.acorn.elearning.analysis.view;

import java.util.Map;

public record PremiumAccessView(String title, String status, Map<String, Object> attributes) {
    public static PremiumAccessView stub(String title) { return new PremiumAccessView(title, "SKELETON", Map.of()); }
}
