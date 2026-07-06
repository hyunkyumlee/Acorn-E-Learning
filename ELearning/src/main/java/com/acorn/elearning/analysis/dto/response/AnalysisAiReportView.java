package com.acorn.elearning.analysis.dto.response;

import com.acorn.elearning.common.ai.AiGeneratedTextSanitizer;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

public record AnalysisAiReportView(
        boolean ready,
        String status,
        String freeSummary,
        List<Section> sections
) {
    public static AnalysisAiReportView empty() {
        return new AnalysisAiReportView(false, "대기", "", List.of());
    }

    public static AnalysisAiReportView from(AnalysisReportResponse report, ObjectMapper objectMapper) {
        if (report == null || !"SUCCESS".equals(report.status())) {
            return empty();
        }
        return new AnalysisAiReportView(
                true,
                "분석 완료",
                AiGeneratedTextSanitizer.cleanUserFacingAiText(fallback(report.freeSummary(), "")),
                sections(report.premiumDetail(), objectMapper));
    }

    public record Section(String title, String badge, List<String> items) {
        public Section {
            items = items == null ? List.of() : List.copyOf(items);
        }
    }

    private static List<Section> sections(String premiumDetail, ObjectMapper objectMapper) {
        if (premiumDetail == null || premiumDetail.isBlank()) {
            return List.of();
        }
        try {
            JsonNode root = objectMapper.readTree(premiumDetail);
            if (root.isTextual()) {
                return List.of(new Section("AI 상세 코멘트", "코멘트", List.of(root.asText())));
            }
            List<Section> sections = new ArrayList<>();
            addSection(sections, root.path("strengths"), "강점", "강점");
            addSection(sections, root.path("weaknesses"), "보강점", "보강");
            addSection(sections, root.path("nextActions"), "다음 행동", "행동");
            return List.copyOf(sections);
        } catch (JacksonException exception) {
            return List.of();
        }
    }

    private static void addSection(List<Section> sections, JsonNode node, String title, String badge) {
        List<String> items = textItems(node).stream()
                .map(AiGeneratedTextSanitizer::cleanUserFacingAiText)
                .filter(item -> !item.isBlank())
                .toList();
        if (!items.isEmpty()) {
            sections.add(new Section(title, badge, items));
        }
    }

    private static List<String> textItems(JsonNode node) {
        if (!node.isArray()) {
            return List.of();
        }
        List<String> items = new ArrayList<>();
        for (JsonNode item : node) {
            if (item.isTextual() && !item.asText().isBlank()) {
                items.add(item.asText());
            }
        }
        return List.copyOf(items);
    }

    private static String fallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
