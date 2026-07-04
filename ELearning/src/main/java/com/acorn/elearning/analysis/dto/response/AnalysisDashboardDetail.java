package com.acorn.elearning.analysis.dto.response;

import com.acorn.elearning.analysis.model.AnalysisCodingExamAggregate;
import com.acorn.elearning.analysis.model.AnalysisCodingMistakeStat;
import com.acorn.elearning.analysis.model.AnalysisExamSummary;
import com.acorn.elearning.analysis.model.AnalysisPracticeSummary;
import com.acorn.elearning.analysis.model.AnalysisWrongAnswerNodeStat;
import com.acorn.elearning.analysis.model.AnalysisWrongAnswerSummary;
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
                        new PremiumInsight("분석 기준", "채점된 코딩 테스트와 학습 기록이 쌓이면 상세 분석이 열립니다.", "대기"),
                        new PremiumInsight("다음 행동", "먼저 AI 코딩 테스트를 완료해 주세요.", "준비")
                ),
                List.of(),
                RadarChart.empty());
    }

    public static AnalysisDashboardDetail from(
            AnalysisExamSummary latestExam,
            AnalysisCodingExamAggregate codingExamAggregate,
            List<AnalysisCodingMistakeStat> mistakeStats,
            AnalysisPracticeSummary practiceSummary,
            AnalysisWrongAnswerSummary wrongAnswerSummary,
            List<AnalysisWrongAnswerNodeStat> wrongNodeStats,
            String weakPoint,
            int progressRate,
            int codingTestRate,
            int practiceRate,
            int openWrongAnswers
    ) {
        int wrongAnswerResolvedRate = percent(
                wrongAnswerSummary.getSolvedWrongAnswers(),
                wrongAnswerSummary.getTotalWrongAnswers());
        return new AnalysisDashboardDetail(
                weakNodePoints(wrongNodeStats),
                AnalysisMistakeReviewFactory.from(mistakeStats),
                premiumInsights(latestExam, codingExamAggregate, practiceSummary, wrongAnswerSummary, weakPoint, codingTestRate, practiceRate, openWrongAnswers),
                AnalysisDashboardChartFactory.pieCharts(codingExamAggregate, practiceSummary, wrongAnswerSummary, codingTestRate, practiceRate, wrongAnswerResolvedRate),
                radarChart(progressRate, practiceRate, codingTestRate, wrongAnswerResolvedRate, openWrongAnswers));
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

    private static List<WeakNodePoint> weakNodePoints(List<AnalysisWrongAnswerNodeStat> wrongNodeStats) {
        int maxWrongCount = wrongNodeStats.stream()
                .map(AnalysisWrongAnswerNodeStat::getTotalWrongCount)
                .mapToInt(AnalysisDashboardDetail::number)
                .max()
                .orElse(0);
        return wrongNodeStats.stream()
                .map(stat -> {
                    int totalWrongCount = number(stat.getTotalWrongCount());
                    int intensity = maxWrongCount == 0 ? 0 : percent(totalWrongCount, maxWrongCount);
                    return new WeakNodePoint(
                            fallback(stat.getNodeTitle(), "오답 누적 단원"),
                            totalWrongCount,
                            number(stat.getOpenCount()),
                            intensity);
                })
                .toList();
    }

    private static List<PremiumInsight> premiumInsights(
            AnalysisExamSummary latestExam,
            AnalysisCodingExamAggregate codingExamAggregate,
            AnalysisPracticeSummary practiceSummary,
            AnalysisWrongAnswerSummary wrongAnswerSummary,
            String weakPoint,
            int codingTestRate,
            int practiceRate,
            int openWrongAnswers
    ) {
        return List.of(
                new PremiumInsight(
                        "현재 강점",
                        strengthDescription(latestExam, codingExamAggregate, codingTestRate, practiceRate),
                        codingTestRate >= 70 ? "안정" : "보강"),
                new PremiumInsight(
                        "집중 보강",
                        weakPoint + " 흐름을 먼저 복습하면 다음 코딩 테스트 안정성이 올라갑니다.",
                        openWrongAnswers > 0 ? "오답 우선" : "학습 우선"),
                new PremiumInsight(
                        "문제풀이 리듬",
                        number(practiceSummary.getPassedAttempts()) + "회 통과, 누적 오답 "
                                + number(wrongAnswerSummary.getTotalWrongCount()) + "회 기준으로 복습 순서를 잡습니다.",
                        practiceRate + "%"),
                new PremiumInsight(
                        "다음 액션",
                        AnalysisDashboardResponse.recommendation(weakPoint, codingTestRate, openWrongAnswers),
                        "추천")
        );
    }

    private static String strengthDescription(AnalysisExamSummary exam, AnalysisCodingExamAggregate codingExamAggregate, int codingTestRate, int practiceRate) {
        String subjectName = fallback(exam.getSubjectName(), "최근 과목");
        if (codingTestRate >= 90) {
            return subjectName + " 누적 코딩 테스트 정답률이 높습니다. "
                    + number(codingExamAggregate.getTotalExamCount()) + "회 응시 기준으로 다음 레벨 준비가 되어 있습니다.";
        }
        if (codingTestRate >= 70) {
            return subjectName + " 핵심 흐름은 잡혀 있습니다. 누적 실패 유형만 빠르게 좁히면 됩니다.";
        }
        if (practiceRate >= 70) {
            return "일반 문제풀이 흐름은 괜찮지만 누적 코딩 테스트 적용에서 흔들림이 있습니다.";
        }
        return "기초 개념과 문제풀이 리듬을 함께 다시 잡는 것이 좋습니다.";
    }

    private static RadarChart radarChart(
            int progressRate,
            int practiceRate,
            int codingTestRate,
            int wrongAnswerResolvedRate,
            int openWrongAnswers
    ) {
        int reviewBalance = Math.max(0, 100 - Math.min(openWrongAnswers * 15, 100));
        List<RadarAxis> axes = List.of(
                radarAxis("학습 진행", progressRate, 0),
                radarAxis("일반 문제", practiceRate, 1),
                radarAxis("코딩 테스트", codingTestRate, 2),
                radarAxis("오답 해결", wrongAnswerResolvedRate, 3),
                radarAxis("복습 균형", reviewBalance, 4)
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
            case "학습 진행" -> 0;
            case "일반 문제" -> 1;
            case "코딩 테스트" -> 2;
            case "오답 해결" -> 3;
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
