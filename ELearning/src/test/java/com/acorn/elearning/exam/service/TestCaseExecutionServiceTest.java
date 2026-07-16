package com.acorn.elearning.exam.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.acorn.elearning.exam.dto.response.ExamCodeRunResponse;
import com.acorn.elearning.exam.dto.response.TestCaseExecutionResult;
import com.acorn.elearning.exam.model.AiExamProblem;
import com.acorn.elearning.exam.model.ExamAnswer;
import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class TestCaseExecutionServiceTest {

    private final TestCaseExecutionService service = new TestCaseExecutionService(new ObjectMapper());

    @Test
    void execute_marks_answer_passed_when_java_solution_matches_all_test_cases() {
        AiExamProblem problem = new AiExamProblem();
        problem.setTestCaseSpec(twoCaseSpec());
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

    @Test
    void executeRaw_returns_system_output_for_standard_input() {
        TestCaseExecutionService.CodeExecutionResult result = service.executeRaw("""
                import java.util.Scanner;

                public class Solution {
                    public static void main(String[] args) {
                        Scanner scanner = new Scanner(System.in);
                        System.out.println(scanner.nextInt() + scanner.nextInt());
                    }
                }
                """, "10 32");

        assertTrue(result.success());
        assertEquals("SUCCESS", result.status());
        assertEquals("42\n", result.output());
    }

    @Test
    void executeRaw_blocks_file_access_before_compilation() {
        TestCaseExecutionService.CodeExecutionResult result = service.executeRaw("""
                import java.nio.file.Files;

                public class Solution {
                    public static void main(String[] args) {
                        System.out.println(Files.exists(null));
                    }
                }
                """, "");

        assertFalse(result.success());
        assertEquals("SECURITY_VIOLATION", result.status());
        assertTrue(result.output().contains("허용되지 않는 API"));
    }

    @Test
    void executeRaw_blocks_method_handle_api_before_compilation() {
        TestCaseExecutionService.CodeExecutionResult result = service.executeRaw("""
                public class Solution {
                    public static void main(String[] args) {
                        java.lang.invoke.MethodHandles.lookup();
                    }
                }
                """, "");

        assertFalse(result.success());
        assertEquals("SECURITY_VIOLATION", result.status());
        assertTrue(result.output().contains("허용되지 않는 API"));
    }

    @Test
    void executeRaw_blocks_repeated_u_unicode_escape_before_compilation() {
        TestCaseExecutionService.CodeExecutionResult result = service.executeRaw("""
                public class Solution {
                    public static void main(String[] args) {
                        java.lang.invoke.\\uu004dethodHandles.lookup();
                    }
                }
                """, "");

        assertFalse(result.success());
        assertEquals("SECURITY_VIOLATION", result.status());
        assertTrue(result.output().contains("허용되지 않는 API"));
    }

    @Test
    void execute_blocks_file_access_api_before_compilation() {
        AiExamProblem problem = new AiExamProblem();
        problem.setTestCaseSpec(twoCaseSpec());
        ExamAnswer answer = new ExamAnswer();
        answer.setAnswerText("""
                import java.nio.file.Files;
                import java.nio.file.Path;

                public class Solution {
                    public static void main(String[] args) throws Exception {
                        System.out.println(Files.readString(Path.of("/etc/passwd")));
                    }
                }
                """);

        TestCaseExecutionResult result = service.execute(problem, answer);

        assertEquals("SECURITY_VIOLATION", result.status());
        assertFalse(result.passed());
        assertEquals(0, result.passedCount());
        assertTrue(result.cases().get(0).errorMessage().contains("허용되지 않는 API"));
    }

    @Test
    void execute_blocks_process_api_before_compilation() {
        AiExamProblem problem = new AiExamProblem();
        problem.setTestCaseSpec(twoCaseSpec());
        ExamAnswer answer = new ExamAnswer();
        answer.setAnswerText("""
                public class Solution {
                    public static void main(String[] args) throws Exception {
                        Runtime.getRuntime().exec("whoami");
                    }
                }
                """);

        TestCaseExecutionResult result = service.execute(problem, answer);

        assertEquals("SECURITY_VIOLATION", result.status());
        assertFalse(result.passed());
        assertTrue(result.cases().get(0).errorMessage().contains("허용되지 않는 API"));
    }

    @Test
    void execute_blocks_process_api_when_runtime_call_uses_whitespace() {
        AiExamProblem problem = new AiExamProblem();
        problem.setTestCaseSpec(twoCaseSpec());
        ExamAnswer answer = new ExamAnswer();
        answer.setAnswerText("""
                public class Solution {
                    public static void main(String[] args) throws Exception {
                        Runtime . getRuntime() . exec("whoami");
                    }
                }
                """);

        TestCaseExecutionResult result = service.execute(problem, answer);

        assertEquals("SECURITY_VIOLATION", result.status());
        assertFalse(result.passed());
        assertTrue(result.cases().get(0).errorMessage().contains("허용되지 않는 API"));
    }

    @Test
    void execute_blocks_unicode_escape_source_before_compilation() {
        AiExamProblem problem = new AiExamProblem();
        problem.setTestCaseSpec(twoCaseSpec());
        ExamAnswer answer = new ExamAnswer();
        answer.setAnswerText("""
                public class Solution {
                    public static void main(String[] args) throws Exception {
                        \\u0052untime.getRuntime().exec("whoami");
                    }
                }
                """);

        TestCaseExecutionResult result = service.execute(problem, answer);

        assertEquals("SECURITY_VIOLATION", result.status());
        assertFalse(result.passed());
        assertTrue(result.cases().get(0).errorMessage().contains("허용되지 않는 API"));
    }

    @Test
    void codeRunResponse_uses_security_violation_message() {
        TestCaseExecutionResult result = new TestCaseExecutionResult(
                "SECURITY_VIOLATION",
                false,
                0,
                1,
                List.of(new TestCaseExecutionResult.CaseResult("", "", "", false, "차단")));

        ExamCodeRunResponse response = ExamCodeRunResponse.from(result, 1L);

        assertEquals("보안 정책으로 실행이 차단되었습니다.", response.message());
    }

    private String twoCaseSpec() {
        return """
                {
                  "cases": [
                    {"input": "1 2", "expectedOutput": "3"},
                    {"input": "10 15", "expectedOutput": "25"}
                  ]
                }
                """;
    }
}
