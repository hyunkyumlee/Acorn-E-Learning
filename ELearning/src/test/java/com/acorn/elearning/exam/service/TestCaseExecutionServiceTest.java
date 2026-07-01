package com.acorn.elearning.exam.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.acorn.elearning.exam.dto.response.TestCaseExecutionResult;
import com.acorn.elearning.exam.model.AiExamProblem;
import com.acorn.elearning.exam.model.ExamAnswer;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class TestCaseExecutionServiceTest {

    private final TestCaseExecutionService service = new TestCaseExecutionService(new ObjectMapper());

    @Test
    void execute_marks_answer_passed_when_java_solution_matches_all_test_cases() {
        AiExamProblem problem = new AiExamProblem();
        problem.setTestCaseSpec("""
                {
                  "cases": [
                    {"input": "1 2", "expectedOutput": "3"},
                    {"input": "10 15", "expectedOutput": "25"}
                  ]
                }
                """);
        ExamAnswer answer = new ExamAnswer();
        answer.setAnswerText("""
                import java.util.Scanner;

                public class Solution {
                    public static void main(String[] args) {
                        Scanner scanner = new Scanner(System.in);
                        int a = scanner.nextInt();
                        int b = scanner.nextInt();
                        System.out.println(a + b);
                    }
                }
                """);

        TestCaseExecutionResult result = service.execute(problem, answer);

        assertTrue(result.passed());
        assertEquals(2, result.passedCount());
        assertEquals(2, result.totalCount());
    }
}
