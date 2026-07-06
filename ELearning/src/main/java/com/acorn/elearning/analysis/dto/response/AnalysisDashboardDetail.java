package com.acorn.elearning.analysis.dto.response;

import com.acorn.elearning.analysis.model.AnalysisCodingExamAggregate;
import com.acorn.elearning.analysis.model.AnalysisCodingMistakeStat;
import com.acorn.elearning.analysis.model.AnalysisExamSummary;
import com.acorn.elearning.analysis.model.AnalysisSubjectSummary;
import java.util.List;
import java.util.Locale;

public record AnalysisDashboardDetail(
        List<WeakNodePoint> weakNodes,
        List<MistakeReview> mistakeReviews,
        List<PremiumInsight> premiumInsights,
        List<PieChart> pieCharts,
        RadarChart radarChart
) {
    public static AnalysisDashboardDetail empty() {
        return new AnalysisDashboardDetail(
                List.of(),
                List.of(),
                List.of(
                        new PremiumInsight("분석 기준", "채점된 코딩테스트 기록이 쌓이면 상세 분석이 열립니다.", "대기"),
                        new PremiumInsight("다음 행동", "먼저 AI 코딩테스트를 완료해 주세요.", "준비")
                ),
                List.of(),
                RadarChart.empty());
    }

    public static AnalysisDashboardDetail from(
            AnalysisExamSummary latestExam,
            AnalysisCodingExamAggregate codingExamAggregate,
            List<AnalysisCodingMistakeStat> mistakeStats,
            List<AnalysisSubjectSummary> subjectSummaries,
            String weakPoint,
            int codingTestRate,
            int passRate,
            int recentTrendRate,
            int failedProblemCount,
            int retryCount
    ) {
        int mistakeStability = mistakeStability(failedProblemCount, codingExamAggregate.getTotalProblemCount());
        int retryControl = Math.max(0, 100 - Math.min(retryCount * 15, 100));
        return new AnalysisDashboardDetail(
                weakNodePoints(subjectSummaries),
                AnalysisMistakeReviewFactory.from(mistakeStats),
                premiumInsights(latestExam, codingExamAggregate, weakPoint, codingTestRate, passRate, failedProblemCount, retryCount),
                AnalysisDashboardChartFactory.pieCharts(codingExamAggregate, codingTestRate, passRate, failedProblemCount),
                radarChart(codingTestRate, passRate, recentTrendRate, mistakeStability, retryControl));
    }

    public record WeakNodePoint(String label, int totalWrongCount, int openCount, int intensity) {}

    public record MistakeReview(
            String title,
            String badge,
            String description,
            int occurrenceCount,
            int affectedExamCount,
            String impactLabel,
            String actionLabel,
            int intensity
    ) {}

    public record PremiumInsight(String title, String description, String badge) {}

    public record PieChart(
            String title,
            int primaryRate,
            String primaryLabel,
            String secondaryLabel,
            String caption
    ) {}

    public record RadarAxis(String label, int value, int x, int y, int labelX, int labelY) {}

    public record RadarChart(List<RadarAxis> axes, String polygonPoints) {
        static RadarChart empty() {
            return new RadarChart(List.of(), "");
        }
    }

    private static List<WeakNodePoint> weakNodePoints(List<AnalysisSubjectSummary> subjectSummaries) {
        int maxFailedCount = subjectSummaries.stream()
                .map(AnalysisSubjectSummary::getFailedProblemCount)
                .mapToInt(AnalysisDashboardDetail::number)
                .max()
                .orElse(0);
        return subjectSummaries.stream()
                .filter(summary -> number(summary.getFailedProblemCount()) > 0)
                .map(summary -> {
                    int failedProblemCount = number(summary.getFailedProblemCount());
                    int intensity = maxFailedCount == 0 ? 0 : percent(failedProblemCount, maxFailedCount);
                    return new WeakNodePoint(
                            fallback(summary.getSubjectName(), "과목 미지정"),
                            failedProblemCount,
                            number(summary.getCodingExamCount()),
                            intensity);
                })
                .toList();
    }

    private static List<PremiumInsight> premiumInsights(
            AnalysisExamSummary latestExam,
            AnalysisCodingExamAggregate codingExamAggregate,
            String weakPoint,
            int codingTestRate,
            int passRate,
            int failedProblemCount,
            int retryCount
    ) {
        return List.of(
                new PremiumInsight(
                        "누적 성과",
                        strengthDescription(latestExam, codingExamAggregate, codingTestRate, passRate),
                        codingTestRate >= 70 ? "안정" : "보강"),
                new PremiumInsight(
                        "집중 보강",
                        weakPoint + "에서 틀린 문항이 반복되는지 먼저 확인하면 다음 코딩테스트 안정성이 올라갑니다.",
                        failedProblemCount > 0 ? "재확인" : "안정"),
                new PremiumInsight(
                        "재응시 흐름",
                        number(codingExamAggregate.getTotalExamCount()) + "회 응시, "
                                + retryCount + "회 재응시 기준으로 풀이 안정성을 봅니다.",
                        retryCount > 0 ? "점검" : "양호"),
                new PremiumInsight(
                        "다음 액션",
                        AnalysisDashboardResponse.recommendation(weakPoint, codingTestRate, passRate, failedProblemCount),
                        "추천")
        );
    }

    private static String strengthDescription(
            AnalysisExamSummary exam,
            AnalysisCodingExamAggregate codingExamAggregate,
            int codingTestRate,
            int passRate
    ) {
        String subjectName = fallback(exam.getSubjectName(), "최근 과목");
        if (codingTestRate >= 90 && passRate >= 80) {
            return subjectName + " 포함 누적 코딩테스트 정답률이 높습니다. "
                    + number(codingExamAggregate.getTotalExamCount()) + "회 응시 기준으로 다음 레벨 준비가 되어 있습니다.";
        }
        if (codingTestRate >= 70) {
            return "누적 코딩테스트 핵심 흐름은 잡혀 있습니다. 통과하지 못한 코딩테스트의 실패 유형만 좁히면 됩니다.";
        }
        return "누적 코딩테스트 정답률이 낮아 구현 로직, 출력 형식, 조건 처리 순서로 다시 점검하는 것이 좋습니다.";
    }

    private static RadarChart radarChart(
            int codingTestRate,
            int passRate,
            int recentTrendRate,
            int mistakeStability,
            int retryControl
    ) {
        List<RadarAxis> axes = List.of(
                radarAxis("정답률", codingTestRate, 0),
                radarAxis("통과율", passRate, 1),
                radarAxis("최근 흐름", recentTrendRate, 2),
                radarAxis("실수 안정", mistakeStability, 3),
                radarAxis("재응시 관리", retryControl, 4)
        );
        String polygonPoints = axes.stream()
                .map(axis -> point(axis.value(), axisIndex(axis.label()), 84))
                .reduce((left, right) -> left + " " + right)
                .orElse("");
        return new RadarChart(axes, polygonPoints);
    }

    private static RadarAxis radarAxis(String label, int value, int index) {
        int labelRadius = 102;
        double angle = angle(index);
        return new RadarAxis(
                label,
                clampPercent(value),
                coordinate(110, 84, angle),
                coordinate(110, 84, angle, false),
                coordinate(110, labelRadius, angle),
                coordinate(110, labelRadius, angle, false));
    }

    private static int axisIndex(String label) {
        return switch (label) {
            case "정답률" -> 0;
            case "통과율" -> 1;
            case "최근 흐름" -> 2;
            case "실수 안정" -> 3;
            default -> 4;
        };
    }

    private static String point(int value, int index, int radius) {
        double angle = angle(index);
        double scaledRadius = radius * clampPercent(value) / 100.0;
        return String.format(
                Locale.US,
                "%.1f,%.1f",
                110 + Math.sin(angle) * scaledRadius,
                110 - Math.cos(angle) * scaledRadius);
    }

    private static double angle(int index) {
        return Math.toRadians(index * 72.0);
    }

    private static int coordinate(int center, int radius, double angle) {
        return coordinate(center, radius, angle, true);
    }

    private static int coordinate(int center, int radius, double angle, boolean xAxis) {
        double offset = xAxis ? Math.sin(angle) * radius : -Math.cos(angle) * radius;
        return (int) Math.round(center + offset);
    }

    private static int mistakeStability(int failedProblemCount, Integer totalProblemCount) {
        if (totalProblemCount == null || totalProblemCount <= 0) {
            return 100;
        }
        return clampPercent(100 - percent(failedProblemCount, totalProblemCount));
    }

    private static int percent(Integer numerator, Integer denominator) {
        if (numerator == null || denominator == null || denominator <= 0) {
            return 0;
        }
        return clampPercent((int) Math.round(numerator * 100.0 / denominator));
    }

    private static int clampPercent(int value) {
        if (value < 0) {
            return 0;
        }
        return Math.min(value, 100);
    }

    private static int number(Integer value) {
        return value == null ? 0 : value;
    }

    private static String fallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
