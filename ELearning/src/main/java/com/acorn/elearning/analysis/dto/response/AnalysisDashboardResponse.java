package com.acorn.elearning.analysis.dto.response;

import com.acorn.elearning.analysis.model.AnalysisCodingExamAggregate;
import com.acorn.elearning.analysis.model.AnalysisCodingMistakeStat;
import com.acorn.elearning.analysis.model.AnalysisExamSummary;
import com.acorn.elearning.analysis.model.AnalysisLearningProgressStat;
import com.acorn.elearning.analysis.model.AnalysisLevelSummary;
import com.acorn.elearning.analysis.model.AnalysisPracticeSummary;
import com.acorn.elearning.analysis.model.AnalysisSubjectSummary;
import com.acorn.elearning.analysis.model.AnalysisWrongAnswerNodeStat;
import com.acorn.elearning.analysis.model.AnalysisWrongAnswerSummary;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public record AnalysisDashboardResponse(
        boolean hasExam,
        boolean premiumActive,
        AnalysisReportResponse report,
        AnalysisAiReportView aiReport,
        ExamOverview latestExam,
        List<Metric> metrics,
        String freeSummary,
        String weakPoint,
        String recommendation,
        List<TrendPoint> trend,
        List<ProgressPoint> progress,
        AnalysisDashboardDetail detail,
        List<SubjectOverview> subjectSummaries,
        List<LevelOverview> levelSummaries
) {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    public static AnalysisDashboardResponse empty(
            boolean premiumActive,
            AnalysisReportResponse report,
            AnalysisAiReportView aiReport,
            List<AnalysisSubjectSummary> subjectSummaries,
            List<AnalysisLevelSummary> levelSummaries
    ) {
        return new AnalysisDashboardResponse(
                false,
                premiumActive,
                report,
                aiReport,
                ExamOverview.empty(),
                List.of(
                        new Metric("학습 진행", "0%", "학습 기록 없음"),
                        new Metric("일반 문제 정답률", "0%", "풀이 기록 없음"),
                        new Metric("누적 코딩테스트 정답률", "0%", "채점 완료 기록 없음"),
                        new Metric("코딩테스트 통과율", "0%", "통과 기록 없음")
                ),
                "채점 완료된 AI 코딩테스트가 아직 없습니다. 먼저 코딩테스트를 응시한 뒤 분석을 생성해 주세요.",
                "분석 가능한 코딩테스트 기록 없음",
                "AI 코딩테스트를 완료하면 누적 코딩테스트 기준으로 추천을 제공합니다.",
                List.of(),
                List.of(),
                AnalysisDashboardDetail.empty(),
                subjectOverviews(subjectSummaries),
                levelOverviews(levelSummaries)
        );
    }

    public static AnalysisDashboardResponse from(
            boolean premiumActive,
            AnalysisReportResponse report,
            AnalysisAiReportView aiReport,
            AnalysisExamSummary latestExam,
            AnalysisCodingExamAggregate codingExamAggregate,
            List<AnalysisCodingMistakeStat> mistakeStats,
            List<AnalysisExamSummary> recentExams,
            List<AnalysisLearningProgressStat> progressStats,
            AnalysisPracticeSummary practiceSummary,
            AnalysisWrongAnswerSummary wrongAnswerSummary,
            List<AnalysisWrongAnswerNodeStat> wrongNodeStats,
            List<AnalysisSubjectSummary> subjectSummaries,
            List<AnalysisLevelSummary> levelSummaries
    ) {
        int latestExamScoreRate = percent(latestExam.getCorrectCount(), latestExam.getTotalProblemCount());
        int codingTestRate = percent(codingExamAggregate.getCorrectCount(), codingExamAggregate.getTotalProblemCount());
        int passRate = percent(codingExamAggregate.getPassedExamCount(), codingExamAggregate.getTotalExamCount());
        int progressRate = averageProgress(progressStats);
        int practiceRate = practiceSummary.getAverageCorrectRate() == null
                ? 0
                : clampPercent(practiceSummary.getAverageCorrectRate().setScale(0, RoundingMode.HALF_UP).intValue());
        int failedProblemCount = Math.max(0,
                number(codingExamAggregate.getTotalProblemCount()) - number(codingExamAggregate.getCorrectCount()));
        String weakPoint = codingWeakPoint(subjectSummaries, levelSummaries, mistakeStats);

        return new AnalysisDashboardResponse(
                true,
                premiumActive,
                report,
                aiReport,
                ExamOverview.from(latestExam, latestExamScoreRate),
                List.of(
                        new Metric("학습 진행", progressRate + "%", completedProgressLabel(progressStats)),
                        new Metric("일반 문제 정답률", practiceRate + "%", practiceCorrectLabel(practiceSummary)),
                        new Metric("누적 코딩테스트 정답률", codingTestRate + "%", codingTestCorrectLabel(codingExamAggregate)),
                        new Metric("코딩테스트 통과율", passRate + "%", codingTestPassLabel(codingExamAggregate))
                ),
                freeSummary(aiReport, codingExamAggregate, codingTestRate, passRate, weakPoint),
                weakPoint,
                recommendation(weakPoint, codingTestRate, passRate, failedProblemCount),
                trendPoints(recentExams),
                progressPoints(progressStats),
                AnalysisDashboardDetail.from(
                        latestExam,
                        codingExamAggregate,
                        mistakeStats,
                        subjectSummaries,
                        weakPoint,
                        codingTestRate,
                        passRate,
                        recentTrendRate(recentExams),
                        failedProblemCount,
                        number(codingExamAggregate.getRetryCount())),
                subjectOverviews(subjectSummaries),
                levelOverviews(levelSummaries)
        );
    }

    public record Metric(String label, String value, String note) {}

    public record SubjectOverview(
            Long subjectId,
            String subjectCode,
            String subjectName,
            int codingTestRate,
            int passRate,
            int examCount,
            int failedProblemCount,
            int retryCount,
            String correctLabel,
            String passLabel,
            String failedLabel,
            String recommendation
    ) {
        static SubjectOverview from(AnalysisSubjectSummary summary) {
            int codingTestRate = percent(summary.getCodingCorrectProblems(), summary.getCodingTotalProblems());
            int passRate = percent(summary.getPassedExamCount(), summary.getCodingExamCount());
            int failedProblemCount = number(summary.getFailedProblemCount());
            int retryCount = number(summary.getRetryCount());
            return new SubjectOverview(
                    summary.getSubjectId(),
                    fallback(summary.getSubjectCode(), "-"),
                    fallback(summary.getSubjectName(), "과목 미지정"),
                    codingTestRate,
                    passRate,
                    number(summary.getCodingExamCount()),
                    failedProblemCount,
                    retryCount,
                    number(summary.getCodingCorrectProblems()) + " / " + number(summary.getCodingTotalProblems()) + " 정답",
                    number(summary.getPassedExamCount()) + " / " + number(summary.getCodingExamCount()) + "회 통과",
                    "틀린 문항 " + failedProblemCount + "개 · 재응시 " + retryCount + "회",
                    codingRecommendation(codingTestRate, passRate, failedProblemCount, retryCount));
        }
    }

    public record LevelOverview(
            String levelCode,
            int codingTestRate,
            int passRate,
            int examCount,
            int failedProblemCount,
            String correctLabel,
            String passLabel,
            String recommendation
    ) {
        static LevelOverview from(AnalysisLevelSummary summary) {
            int codingTestRate = percent(summary.getCodingCorrectProblems(), summary.getCodingTotalProblems());
            int passRate = percent(summary.getPassedExamCount(), summary.getCodingExamCount());
            int failedProblemCount = number(summary.getFailedProblemCount());
            return new LevelOverview(
                    fallback(summary.getLevelCode(), "-"),
                    codingTestRate,
                    passRate,
                    number(summary.getCodingExamCount()),
                    failedProblemCount,
                    number(summary.getCodingCorrectProblems()) + " / " + number(summary.getCodingTotalProblems()) + " 정답",
                    number(summary.getPassedExamCount()) + " / " + number(summary.getCodingExamCount()) + "회 통과",
                    codingRecommendation(codingTestRate, passRate, failedProblemCount, number(summary.getRetryCount())));
        }
    }

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
            return new ExamOverview(null, "최근 코딩테스트 없음", "-", "-", "-", "대기", "0점", "0 / 0", "-", 0);
        }

        static ExamOverview from(AnalysisExamSummary exam, int scoreRate) {
            return new ExamOverview(
                    exam.getExamId(),
                    "코딩테스트 #" + exam.getExamId(),
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
            AnalysisAiReportView aiReport,
            AnalysisCodingExamAggregate codingExamAggregate,
            int codingTestRate,
            int passRate,
            String weakPoint
    ) {
        if (aiReport != null && aiReport.ready() && aiReport.freeSummary() != null && !aiReport.freeSummary().isBlank()) {
            return aiReport.freeSummary();
        }
        return "전체 누적 코딩테스트 정답률은 " + codingTestRate + "%, 통과율은 " + passRate + "%입니다. "
                + number(codingExamAggregate.getTotalExamCount()) + "회 코딩테스트에서 "
                + number(codingExamAggregate.getCorrectCount()) + " / "
                + number(codingExamAggregate.getTotalProblemCount()) + "문제를 맞혔고, "
                + weakPoint + " 흐름을 먼저 점검하면 좋습니다.";
    }

    private static List<SubjectOverview> subjectOverviews(List<AnalysisSubjectSummary> subjectSummaries) {
        return subjectSummaries.stream()
                .map(SubjectOverview::from)
                .toList();
    }

    private static List<LevelOverview> levelOverviews(List<AnalysisLevelSummary> levelSummaries) {
        return levelSummaries.stream()
                .map(LevelOverview::from)
                .toList();
    }

    private static String codingRecommendation(int codingTestRate, int passRate, int failedProblemCount, int retryCount) {
        if (failedProblemCount > 0 && codingTestRate < 70) {
            return "틀린 문항이 누적되어 있어 구현 로직과 출력 형식을 먼저 다시 확인해 주세요.";
        }
        if (passRate < 70) {
            return "통과한 코딩테스트 비율이 낮아 같은 난이도에서 한 번 더 안정화하는 것이 좋습니다.";
        }
        if (retryCount > 0) {
            return "재응시 기록이 있어 이전 실수 유형을 확인한 뒤 다음 코딩테스트로 넘어가세요.";
        }
        return "누적 코딩테스트 흐름이 안정적입니다.";
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

    private static String codingWeakPoint(
            List<AnalysisSubjectSummary> subjectSummaries,
            List<AnalysisLevelSummary> levelSummaries,
            List<AnalysisCodingMistakeStat> mistakeStats
    ) {
        return subjectSummaries.stream()
                .max(Comparator.comparingInt(AnalysisDashboardResponse::subjectRiskScore))
                .filter(summary -> number(summary.getFailedProblemCount()) > 0)
                .map(summary -> fallback(summary.getSubjectName(), "과목 미지정") + " 과목")
                .or(() -> levelSummaries.stream()
                        .max(Comparator.comparingInt(AnalysisDashboardResponse::levelRiskScore))
                        .filter(summary -> number(summary.getFailedProblemCount()) > 0)
                        .map(summary -> fallback(summary.getLevelCode(), "난이도 미지정") + " 난이도"))
                .or(() -> mistakeStats.stream()
                        .findFirst()
                        .map(stat -> mistakeTypeLabel(stat.getMistakeType())))
                .orElse("뚜렷한 약점 없음");
    }

    static String recommendation(String weakPoint, int codingTestRate, int passRate, int failedProblemCount) {
        if (failedProblemCount > 0) {
            return weakPoint + "에서 틀린 코딩테스트 문항을 먼저 다시 확인해 주세요.";
        }
        if (codingTestRate < 70) {
            return "누적 코딩테스트 정답률이 낮습니다. 같은 난이도 코딩테스트를 한 번 더 풀어 주세요.";
        }
        if (passRate < 100) {
            return "통과하지 못한 코딩테스트의 반복 실수 유형을 다시 확인한 뒤 다음 단계로 넘어가세요.";
        }
        return "현재 코딩테스트 흐름이 좋습니다. 다음 레벨 학습으로 넘어가도 좋습니다.";
    }

    private static String completedProgressLabel(List<AnalysisLearningProgressStat> progressStats) {
        long completed = progressStats.stream()
                .filter(stat -> Boolean.TRUE.equals(stat.getLessonCompleted()) && Boolean.TRUE.equals(stat.getPracticePassed()))
                .count();
        return completed + " / " + progressStats.size() + "개 단원 완료";
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

    private static String codingTestPassLabel(AnalysisCodingExamAggregate aggregate) {
        return number(aggregate.getPassedExamCount()) + " / " + number(aggregate.getTotalExamCount()) + "회 통과";
    }

    private static int recentTrendRate(List<AnalysisExamSummary> recentExams) {
        return (int) Math.round(recentExams.stream()
                .mapToInt(exam -> percent(exam.getCorrectCount(), exam.getTotalProblemCount()))
                .average()
                .orElse(0d));
    }

    private static int subjectRiskScore(AnalysisSubjectSummary summary) {
        return number(summary.getFailedProblemCount()) * 4
                + (100 - percent(summary.getCodingCorrectProblems(), summary.getCodingTotalProblems()))
                + (100 - percent(summary.getPassedExamCount(), summary.getCodingExamCount()))
                + number(summary.getRetryCount()) * 2;
    }

    private static int levelRiskScore(AnalysisLevelSummary summary) {
        return number(summary.getFailedProblemCount()) * 4
                + (100 - percent(summary.getCodingCorrectProblems(), summary.getCodingTotalProblems()))
                + (100 - percent(summary.getPassedExamCount(), summary.getCodingExamCount()))
                + number(summary.getRetryCount()) * 2;
    }

    private static int averageProgress(List<AnalysisLearningProgressStat> progressStats) {
        return (int) Math.round(progressStats.stream()
                .map(AnalysisLearningProgressStat::getProgressRate)
                .mapToInt(AnalysisDashboardResponse::percent)
                .average()
                .orElse(0d));
    }

    private static String mistakeTypeLabel(String mistakeType) {
        return switch (fallback(mistakeType, "")) {
            case "OUTPUT_FORMAT" -> "출력 형식";
            case "ARRAY_STRING" -> "배열/문자열 처리";
            case "CONTROL_FLOW" -> "조건문/반복문";
            case "MISSING_CORE_LOGIC" -> "핵심 로직 누락";
            default -> "엣지 케이스";
        };
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
