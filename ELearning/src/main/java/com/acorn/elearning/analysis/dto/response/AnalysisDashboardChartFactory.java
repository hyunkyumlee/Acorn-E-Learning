package com.acorn.elearning.analysis.dto.response;

import com.acorn.elearning.analysis.model.AnalysisCodingExamAggregate;
import java.util.List;

final class AnalysisDashboardChartFactory {
    private AnalysisDashboardChartFactory() {
    }

    static List<AnalysisDashboardDetail.PieChart> pieCharts(
            AnalysisCodingExamAggregate codingExamAggregate,
            int codingTestRate,
            int passRate,
            int failedProblemCount
    ) {
        int failedRate = percent(failedProblemCount, codingExamAggregate.getTotalProblemCount());
        return List.of(
                new AnalysisDashboardDetail.PieChart(
                        "코딩테스트 정답률",
                        codingTestRate,
                        "정답",
                        "오답",
                        number(codingExamAggregate.getCorrectCount()) + " / "
                                + number(codingExamAggregate.getTotalProblemCount()) + " 정답"),
                new AnalysisDashboardDetail.PieChart(
                        "코딩테스트 통과율",
                        passRate,
                        "통과",
                        "재확인",
                        number(codingExamAggregate.getPassedExamCount()) + " / "
                                + number(codingExamAggregate.getTotalExamCount()) + "회 통과"),
                new AnalysisDashboardDetail.PieChart(
                        "재확인 문항 비율",
                        failedRate,
                        "재확인",
                        "통과",
                        failedProblemCount + " / "
                                + number(codingExamAggregate.getTotalProblemCount()) + "문항 재확인")
        );
    }

    private static int percent(Integer numerator, Integer denominator) {
        if (numerator == null || denominator == null || denominator <= 0) {
            return 0;
        }
        return Math.max(0, Math.min(100, (int) Math.round(numerator * 100.0 / denominator)));
    }

    private static int number(Integer value) {
        return value == null ? 0 : value;
    }
}
