package com.acorn.elearning.analysis.dto.response;

import com.acorn.elearning.analysis.model.AnalysisCodingExamAggregate;
import com.acorn.elearning.analysis.model.AnalysisPracticeSummary;
import com.acorn.elearning.analysis.model.AnalysisWrongAnswerSummary;
import java.util.List;

final class AnalysisDashboardChartFactory {
    private AnalysisDashboardChartFactory() {
    }

    static List<AnalysisDashboardDetail.PieChart> pieCharts(
            AnalysisCodingExamAggregate codingExamAggregate,
            AnalysisPracticeSummary practiceSummary,
            AnalysisWrongAnswerSummary wrongAnswerSummary,
            int codingTestRate,
            int practiceRate,
            int wrongAnswerResolvedRate
    ) {
        return List.of(
                new AnalysisDashboardDetail.PieChart(
                        "일반 문제 정답률",
                        practiceRate,
                        "정답",
                        "오답",
                        number(practiceSummary.getCorrectProblems()) + " / "
                                + number(practiceSummary.getTotalProblems()) + " 정답"),
                new AnalysisDashboardDetail.PieChart(
                        "코딩 테스트 정답률",
                        codingTestRate,
                        "정답",
                        "오답",
                        number(codingExamAggregate.getCorrectCount()) + " / "
                                + number(codingExamAggregate.getTotalProblemCount()) + " 정답"),
                new AnalysisDashboardDetail.PieChart(
                        "오답 복습 해결률",
                        wrongAnswerResolvedRate,
                        "해결",
                        "대기",
                        number(wrongAnswerSummary.getSolvedWrongAnswers()) + " / "
                                + number(wrongAnswerSummary.getTotalWrongAnswers()) + " 해결")
        );
    }

    private static int number(Integer value) {
        return value == null ? 0 : value;
    }
}
