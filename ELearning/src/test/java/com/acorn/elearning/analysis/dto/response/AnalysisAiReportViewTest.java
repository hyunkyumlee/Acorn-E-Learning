package com.acorn.elearning.analysis.dto.response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class AnalysisAiReportViewTest {

    @Test
    void from_hides_starter_code_praise_from_strengths() {
        AnalysisReportResponse report = new AnalysisReportResponse(
                1L,
                7L,
                "SUCCESS",
                "누적 코딩테스트 분석입니다.",
                """
                        {
                          "strengths": [
                            "입력값을 읽고 변수와 배열을 선언하는 기본 구조는 대체로 갖추고 계십니다.",
                            "정답 처리된 제출에서는 조건 분기 결과를 정확히 출력하셨습니다."
                          ],
                          "weaknesses": ["출력문이 빠진 제출이 반복됩니다."],
                          "nextActions": ["출력까지 작성했는지 마지막에 확인해 주세요."]
                        }
                        """,
                null,
                0);

        AnalysisAiReportView view = AnalysisAiReportView.from(report, new ObjectMapper());

        assertEquals(3, view.sections().size());
        assertEquals("강점", view.sections().get(0).title());
        assertEquals(1, view.sections().get(0).items().size());
        assertEquals("정답 처리된 제출에서는 조건 분기 결과를 정확히 출력하셨습니다.", view.sections().get(0).items().get(0));
    }
}
