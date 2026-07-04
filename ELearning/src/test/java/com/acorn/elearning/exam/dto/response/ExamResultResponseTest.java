package com.acorn.elearning.exam.dto.response;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.acorn.elearning.exam.model.ExamAnswer;
import com.acorn.elearning.exam.model.ExamSession;
import java.util.List;
import org.junit.jupiter.api.Test;

class ExamResultResponseTest {

    @Test
    void from_parses_ai_review_json_into_readable_fields() {
        ExamSession session = new ExamSession();
        session.setExamId(10L);
        session.setStatus("GRADED");
        session.setResultStatus("FAILED");
        session.setCorrectCount(1);
        session.setTotalProblemCount(3);

        ExamAnswer answer = new ExamAnswer();
        answer.setAnswerId(20L);
        answer.setAiProblemId(30L);
        answer.setPassedCaseCount(2);
        answer.setIsCorrect(false);
        answer.setAiReview("""
                {"explanation":"출력문이 필요합니다.","codeReview":"Scanner 입력은 올바르게 작성하셨습니다."}
                """);

        ExamResultResponse response = ExamResultResponse.from(session, List.of(answer));

        ExamResultResponse.Answer parsedAnswer = response.answers().get(0);
        assertEquals("출력문이 필요합니다.", parsedAnswer.explanation());
        assertEquals("Scanner 입력은 올바르게 작성하셨습니다.", parsedAnswer.codeReview());
    }
}
