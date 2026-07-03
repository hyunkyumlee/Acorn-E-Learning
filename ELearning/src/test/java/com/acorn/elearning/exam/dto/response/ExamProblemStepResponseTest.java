package com.acorn.elearning.exam.dto.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.exam.model.AiExamProblem;
import com.acorn.elearning.exam.model.ExamAnswer;
import com.acorn.elearning.exam.model.ExamSession;
import java.util.List;
import org.junit.jupiter.api.Test;

class ExamProblemStepResponseTest {

    @Test
    void from_returns_current_problem_navigation_when_problem_exists() {
        ExamSessionResponse exam = examResponse();

        ExamProblemStepResponse step = ExamProblemStepResponse.from(exam, 2);

        assertEquals(1, step.previousProblemNo());
        assertEquals(3, step.nextProblemNo());
        assertEquals(1, step.answeredCount());
        assertFalse(step.firstProblem());
        assertFalse(step.lastProblem());
        assertFalse(step.allAnswered());
        assertFalse(step.submittableAfterCurrentSave());
        assertEquals(2, step.problem().problemNo());
        assertFalse(step.problem().answered());
        assertTrue(step.problem().answerText().contains("public class Solution"));
        assertTrue(step.problem().answerText().contains("Scanner"));
        assertFalse(step.problem().answerText().contains("BufferedReader"));
        assertTrue(step.problem().answerText().contains("TODO 여기에 문제 풀이 로직을 작성하세요."));
    }

    @Test
    void from_uses_generated_starter_code_when_ai_response_contains_it() {
        ExamSession session = new ExamSession();
        session.setExamId(10L);
        session.setSubjectId(1L);
        session.setLevelCode("BRONZE");
        session.setStatus("READY");
        session.setTotalProblemCount(1);
        session.setCorrectCount(0);

        AiExamProblem problem = problem(101L, 1);
        problem.setAiRawResponse("""
                {
                  "problems": [
                    {
                      "prompt": "1부터 n까지 합을 구하세요.",
                      "starterCode": "import java.util.Scanner;\\n\\npublic class Solution {\\n    public static void main(String[] args) {\\n        Scanner scanner = new Scanner(System.in);\\n        int n = scanner.nextInt();\\n\\n        // TODO 여기에 문제 풀이 로직을 작성하세요.\\n    }\\n}\\n",
                      "testCases": [
                        {"input": "3", "expectedOutput": "6"}
                      ]
                    }
                  ]
                }
                """);

        ExamSessionResponse exam = ExamSessionResponse.from(session, List.of(problem), List.of());

        assertTrue(exam.problems().get(0).answerText().contains("int n = scanner.nextInt();"));
        assertFalse(exam.problems().get(0).answerText().contains("BufferedReader"));
    }

    @Test
    void from_returns_boundary_navigation_when_first_problem_is_current() {
        ExamSessionResponse exam = examResponse();

        ExamProblemStepResponse step = ExamProblemStepResponse.from(exam, 1);

        assertNull(step.previousProblemNo());
        assertEquals(2, step.nextProblemNo());
        assertTrue(step.firstProblem());
        assertFalse(step.lastProblem());
        assertTrue(step.problem().answered());
    }

    @Test
    void from_returns_submittable_after_current_save_when_current_problem_is_last_unanswered() {
        ExamSession session = new ExamSession();
        session.setExamId(10L);
        session.setSubjectId(1L);
        session.setLevelCode("BRONZE");
        session.setStatus("READY");
        session.setTotalProblemCount(3);
        session.setCorrectCount(0);

        ExamAnswer answer1 = new ExamAnswer();
        answer1.setAiProblemId(101L);
        answer1.setAnswerText("class Solution {}");
        ExamAnswer answer2 = new ExamAnswer();
        answer2.setAiProblemId(102L);
        answer2.setAnswerText("class Solution {}");
        ExamSessionResponse exam = ExamSessionResponse.from(
                session,
                List.of(problem(101L, 1), problem(102L, 2), problem(103L, 3)),
                List.of(answer1, answer2));

        ExamProblemStepResponse step = ExamProblemStepResponse.from(exam, 3);

        assertFalse(step.allAnswered());
        assertTrue(step.submittableAfterCurrentSave());
    }

    @Test
    void from_throws_not_found_when_problem_no_is_missing() {
        ExamSessionResponse exam = examResponse();

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> ExamProblemStepResponse.from(exam, 4));

        assertEquals(ErrorCode.COMMON_NOT_FOUND, exception.errorCode());
        assertEquals("AI 시험 문제를 찾을 수 없습니다.", exception.getMessage());
    }

    private ExamSessionResponse examResponse() {
        ExamSession session = new ExamSession();
        session.setExamId(10L);
        session.setSubjectId(1L);
        session.setLevelCode("BRONZE");
        session.setStatus("READY");
        session.setTotalProblemCount(3);
        session.setCorrectCount(0);

        ExamAnswer answer = new ExamAnswer();
        answer.setAiProblemId(101L);
        answer.setAnswerText("class Solution {}");

        return ExamSessionResponse.from(
                session,
                List.of(problem(101L, 1), problem(102L, 2), problem(103L, 3)),
                List.of(answer));
    }

    private AiExamProblem problem(Long aiProblemId, Integer problemNo) {
        AiExamProblem problem = new AiExamProblem();
        problem.setAiProblemId(aiProblemId);
        problem.setProblemNo(problemNo);
        problem.setPrompt("문제 " + problemNo);
        problem.setStatus("GENERATED");
        return problem;
    }
}
