package com.acorn.elearning.analysis.dto.response;

import com.acorn.elearning.analysis.model.AnalysisCodingMistakeStat;
import com.acorn.elearning.analysis.model.AnalysisCodingExamAggregate;
import com.acorn.elearning.analysis.model.AnalysisExamSummary;
import com.acorn.elearning.analysis.model.AnalysisLearningProgressStat;
import com.acorn.elearning.analysis.model.AnalysisPracticeSummary;
import com.acorn.elearning.analysis.model.AnalysisWrongAnswerNodeStat;
import com.acorn.elearning.analysis.model.AnalysisWrongAnswerSummary;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record AnalysisDashboardResponse(
        boolean hasExam,
        boolean premiumActive,
        AnalysisReportResponse report,
        ExamOverview latestExam,
        List<Metric> metrics,
        String freeSummary,
        String weakPoint,
        String recommendation,
        List<TrendPoint> trend,
        List<ProgressPoint> progress,
        AnalysisDashboardDetail detail
) {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    public static AnalysisDashboardResponse empty(boolean premiumActive, AnalysisReportResponse report) {
        return new AnalysisDashboardResponse(
                false,
                premiumActive,
                report,
                ExamOverview.empty(),
                List.of(
                        new Metric("학습 진행", "0%", "학습 기록 없음"),
                        new Metric("일반 문제 정답률", "0%", "풀이 기록 없음"),
                        new Metric("코딩 테스트 정답률", "0%", "채점 완료 기록 없음"),
                        new Metric("열린 오답", "0개", "복습 대기 없음")
                ),
                "채점 완료된 AI 코딩 테스트가 아직 없습니다. 먼저 코딩 테스트를 응시한 뒤 분석을 생성해 주세요.",
                "분석 가능한 코딩 테스트 기록 없음",
                "AI 코딩 테스트를 완료하면 학습 진행과 오답 흐름을 기준으로 추천을 제공합니다.",
                List.of(),
                List.of(),
                AnalysisDashboardDetail.empty()
        );
    }

    public static AnalysisDashboardResponse from(
            boolean premiumActive,
            AnalysisReportResponse report,
            AnalysisExamSummary latestExam,
            AnalysisCodingExamAggregate codingExamAggregate,
            List<AnalysisCodingMistakeStat> mistakeStats,
            List<AnalysisExamSummary> recentExams,
            List<AnalysisLearningProgressStat> progressStats,
            AnalysisPracticeSummary practiceSummary,
            AnalysisWrongAnswerSummary wrongAnswerSummary,
            List<AnalysisWrongAnswerNodeStat> wrongNodeStats
    ) {
        int latestExamScoreRate = percent(latestExam.getCorrectCount(), latestExam.getTotalProblemCount());
        int codingTestRate = percent(codingExamAggregate.getCorrectCount(), codingExamAggregate.getTotalProblemCount());
        int progressRate = averageProgress(progressStats);
        int practiceRate = practiceSummary.getAverageCorrectRate() == null
                ? 0
                : clampPercent(practiceSummary.getAverageCorrectRate().setScale(0, RoundingMode.HALF_UP).intValue());
        int openWrongAnswers = number(wrongAnswerSummary.getOpenWrongAnswers());
        String weakPoint = weakPoint(wrongNodeStats, progressStats);

        return new AnalysisDashboardResponse(
                true,
                premiumActive,
                report,
                ExamOverview.from(latestExam, latestExamScoreRate),
                List.of(
                        new Metric("학습 진행", progressRate + "%", completedProgressLabel(progressStats)),
                        new Metric("일반 문제 정답률", practiceRate + "%", practiceCorrectLabel(practiceSummary)),
                        new Metric("코딩 테스트 정답률", codingTestRate + "%", codingTestCorrectLabel(codingExamAggregate)),
                        new Metric("열린 오답", openWrongAnswers + "개", wrongAnswerLabel(wrongAnswerSummary))
                ),
                freeSummary(latestExam, codingExamAggregate, codingTestRate, weakPoint),
                weakPoint,
                recommendation(weakPoint, codingTestRate, openWrongAnswers),
                trendPoints(recentExams),
                progressPoints(progressStats),
                AnalysisDashboardDetail.from(
                        latestExam,
                        codingExamAggregate,
                        mistakeStats,
                        practiceSummary,
                        wrongAnswerSummary,
                        wrongNodeStats,
                        weakPoint,
                        progressRate,
                        codingTestRate,
                        practiceRate,
                        openWrongAnswers)
        );
    }

    public record Metric(String label, String value, String note) {}

    public record ExamOverview(
            Long examId,
            String title,
            String subjectName,
            String levelCode,
            String resultStatus,
            String statusLabel,
            String scoreLabel,
            String correctLabel,
            String gradedAtLabel,
            int scoreRate
    ) {
        static ExamOverview empty() {
            return new ExamOverview(null, "최근 코딩 테스트 없음", "-", "-", "-", "대기", "0점", "0 / 0", "-", 0);
        }

        static ExamOverview from(AnalysisExamSummary exam, int scoreRate) {
            return new ExamOverview(
                    exam.getExamId(),
                    "코딩 테스트 #" + exam.getExamId(),
                    fallback(exam.getSubjectName(), "과목 미지정"),
                    fallback(exam.getLevelCode(), "-"),
                    fallback(exam.getResultStatus(), "-"),
                    "PASSED".equals(exam.getResultStatus()) ? "통과" : "재확인",
                    scoreRate + "점",
                    AnalysisDashboardResponse.correctLabel(exam),
                    dateLabel(exam.getGradedAt()),
                    scoreRate
            );
        }
    }

    public record TrendPoint(String label, int scoreRate, String resultStatus) {}

    public record ProgressPoint(String label, int progressRate, boolean completed) {}

    private static String freeSummary(
            AnalysisExamSummary exam,
            AnalysisCodingExamAggregate codingExamAggregate,
            int codingTestRate,
            String weakPoint
    ) {
        return fallback(exam.getSubjectName(), "학습") + " " + fallback(exam.getLevelCode(), "-")
                + " 누적 코딩 테스트 정답률은 " + codingTestRate + "%입니다. "
                + number(codingExamAggregate.getTotalExamCount()) + "회 응시에서 "
                + number(codingExamAggregate.getCorrectCount()) + " / "
                + number(codingExamAggregate.getTotalProblemCount()) + "문제를 맞혔고, "
                + weakPoint + " 흐름을 먼저 점검하면 다음 코딩 테스트 안정성이 올라갑니다.";
    }

    private static List<TrendPoint> trendPoints(List<AnalysisExamSummary> recentExams) {
        List<AnalysisExamSummary> chronological = new ArrayList<>(recentExams);
        Collections.reverse(chronological);
        return chronological.stream()
                .map(exam -> new TrendPoint(
                        "#" + exam.getExamId(),
                        percent(exam.getCorrectCount(), exam.getTotalProblemCount()),
                        fallback(exam.getResultStatus(), "-")))
                .toList();
    }

    private static List<ProgressPoint> progressPoints(List<AnalysisLearningProgressStat> progressStats) {
        return progressStats.stream()
                .filter(stat -> "PLANET".equals(stat.getNodeType()))
                .map(stat -> new ProgressPoint(
                        fallback(stat.getNodeTitle(), "학습 노드"),
                        percent(stat.getProgressRate()),
                        Boolean.TRUE.equals(stat.getLessonCompleted()) && Boolean.TRUE.equals(stat.getPracticePassed())))
                .toList();
    }

    private static String weakPoint(
            List<AnalysisWrongAnswerNodeStat> wrongNodeStats,
            List<AnalysisLearningProgressStat> progressStats
    ) {
        if (!wrongNodeStats.isEmpty()) {
            return fallback(wrongNodeStats.get(0).getNodeTitle(), "오답 누적 단원");
        }
        return progressStats.stream()
                .filter(stat -> percent(stat.getProgressRate()) < 100)
                .findFirst()
                .map(stat -> fallback(stat.getNodeTitle(), "미완료 단원"))
                .orElse("뚜렷한 약점 없음");
    }

    static String recommendation(String weakPoint, int codingTestRate, int openWrongAnswers) {
        if (openWrongAnswers > 0) {
            return weakPoint + " 오답을 먼저 복습해 주세요.";
        }
        if (codingTestRate < 70) {
            return "누적 코딩 테스트 정답률이 낮습니다. 문제 풀이보다 이론 복습을 먼저 진행해 주세요.";
        }
        if (codingTestRate < 100) {
            return "누적 코딩 테스트의 반복 실수 유형을 다시 확인한 뒤 다음 단원으로 넘어가세요.";
        }
        return "현재 흐름이 좋습니다. 다음 레벨 학습으로 넘어가도 좋습니다.";
    }

    private static String completedProgressLabel(List<AnalysisLearningProgressStat> progressStats) {
        long completed = progressStats.stream()
                .filter(stat -> Boolean.TRUE.equals(stat.getLessonCompleted()) && Boolean.TRUE.equals(stat.getPracticePassed()))
                .count();
        return completed + " / " + progressStats.size() + "개 단원 완료";
    }

    private static String wrongAnswerLabel(AnalysisWrongAnswerSummary summary) {
        return "누적 오답 " + number(summary.getTotalWrongCount()) + "회";
    }

    private static String practiceCorrectLabel(AnalysisPracticeSummary summary) {
        return number(summary.getCorrectProblems()) + " / " + number(summary.getTotalProblems()) + " 정답";
    }

    private static String correctLabel(AnalysisExamSummary exam) {
        return number(exam.getCorrectCount()) + " / " + number(exam.getTotalProblemCount()) + " 정답";
    }

    private static String codingTestCorrectLabel(AnalysisCodingExamAggregate aggregate) {
        return "누적 " + number(aggregate.getTotalExamCount()) + "회 · "
                + number(aggregate.getCorrectCount()) + " / "
                + number(aggregate.getTotalProblemCount()) + " 정답";
    }

    private static int averageProgress(List<AnalysisLearningProgressStat> progressStats) {
        return (int) Math.round(progressStats.stream()
                .map(AnalysisLearningProgressStat::getProgressRate)
                .mapToInt(AnalysisDashboardResponse::percent)
                .average()
                .orElse(0d));
    }

    private static int percent(Integer numerator, Integer denominator) {
        if (numerator == null || denominator == null || denominator <= 0) {
            return 0;
        }
        return clampPercent((int) Math.round(numerator * 100.0 / denominator));
    }

    private static int percent(BigDecimal value) {
        if (value == null) {
            return 0;
        }
        return clampPercent(value.setScale(0, RoundingMode.HALF_UP).intValue());
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

    private static String dateLabel(LocalDateTime value) {
        return value == null ? "-" : value.format(DATE_FORMATTER);
    }

    private static String fallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
