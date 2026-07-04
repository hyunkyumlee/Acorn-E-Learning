package com.acorn.elearning.analysis.dto.response;

import com.acorn.elearning.analysis.model.AnalysisCodingMistakeStat;
import java.util.List;

final class AnalysisMistakeReviewFactory {
    private AnalysisMistakeReviewFactory() {
    }

    static List<AnalysisDashboardDetail.MistakeReview> from(List<AnalysisCodingMistakeStat> mistakeStats) {
        int maxOccurrenceCount = mistakeStats.stream()
                .map(AnalysisCodingMistakeStat::getOccurrenceCount)
                .mapToInt(AnalysisMistakeReviewFactory::number)
                .max()
                .orElse(0);
        return mistakeStats.stream()
                .map(stat -> {
                    String mistakeType = fallback(stat.getMistakeType(), "EDGE_CASE");
                    int occurrenceCount = number(stat.getOccurrenceCount());
                    int affectedExamCount = number(stat.getAffectedExamCount());
                    return new AnalysisDashboardDetail.MistakeReview(
                            title(mistakeType),
                            badge(mistakeType),
                            description(mistakeType, stat.getSamplePrompt()),
                            occurrenceCount,
                            affectedExamCount,
                            occurrenceCount + "회 · " + affectedExamCount + "개 코딩 테스트에서 발생",
                            action(mistakeType),
                            maxOccurrenceCount == 0 ? 0 : percent(occurrenceCount, maxOccurrenceCount));
                })
                .toList();
    }

    private static String title(String mistakeType) {
        return switch (mistakeType) {
            case "OUTPUT_FORMAT" -> "출력 형식 불일치";
            case "ARRAY_STRING" -> "배열·문자열 처리 실수";
            case "CONTROL_FLOW" -> "조건문·반복문 흐름 실수";
            case "MISSING_CORE_LOGIC" -> "핵심 구현 누락";
            default -> "경계값·요구사항 누락";
        };
    }

    private static String badge(String mistakeType) {
        return switch (mistakeType) {
            case "OUTPUT_FORMAT" -> "형식";
            case "ARRAY_STRING" -> "자료 처리";
            case "CONTROL_FLOW" -> "흐름";
            case "MISSING_CORE_LOGIC" -> "구현";
            default -> "경계값";
        };
    }

    private static String description(String mistakeType, String samplePrompt) {
        String base = switch (mistakeType) {
            case "OUTPUT_FORMAT" -> "정답 로직보다 출력 문자열, 공백, 문장 형식에서 흔들린 기록이 많습니다.";
            case "ARRAY_STRING" -> "배열이나 문자열을 끝까지 순회하고 비교하는 과정에서 누락이 반복됩니다.";
            case "CONTROL_FLOW" -> "조건 분기나 반복문의 위치가 흔들려 일부 입력에서 결과가 달라집니다.";
            case "MISSING_CORE_LOGIC" -> "입력은 읽었지만 계산, 조건 처리, 출력 중 핵심 구현이 비어 있던 제출이 누적됐습니다.";
            default -> "일부 테스트만 실패한 기록입니다. 경계값과 문제 요구사항을 먼저 다시 확인해야 합니다.";
        };
        String sample = shortText(samplePrompt, 72, "");
        return sample.isBlank() ? base : base + " 대표 문제: " + sample;
    }

    private static String action(String mistakeType) {
        return switch (mistakeType) {
            case "OUTPUT_FORMAT" -> "제출 전 예시 출력과 공백까지 한 줄씩 대조하세요.";
            case "ARRAY_STRING" -> "첫 번째 값만 처리하지 말고 전체 원소를 순회하는지 확인하세요.";
            case "CONTROL_FLOW" -> "조건의 기준값과 반복 시작·종료 범위를 먼저 써놓고 구현하세요.";
            case "MISSING_CORE_LOGIC" -> "입력, 계산, 출력 세 단계를 주석 없이 한 번에 완성하는 연습이 필요합니다.";
            default -> "최솟값, 최댓값, 같은 값, 빈 값 같은 경계 입력을 직접 넣어 보세요.";
        };
    }

    private static String shortText(String text, int maxLength, String fallback) {
        String value = fallback(text, fallback).replaceAll("\\s+", " ").trim();
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...";
    }

    private static int percent(Integer numerator, Integer denominator) {
        if (numerator == null || denominator == null || denominator <= 0) {
            return 0;
        }
        return Math.min((int) Math.round(numerator * 100.0 / denominator), 100);
    }

    private static int number(Integer value) {
        return value == null ? 0 : value;
    }

    private static String fallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
