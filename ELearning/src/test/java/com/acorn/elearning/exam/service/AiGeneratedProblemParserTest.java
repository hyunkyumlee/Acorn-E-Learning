package com.acorn.elearning.exam.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.exam.service.ExamLearningScopeService.ExamLearningScope;
import com.acorn.elearning.exam.service.ExamLearningScopeService.LearnedItem;
import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class AiGeneratedProblemParserTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AiGeneratedProblemParser parser = new AiGeneratedProblemParser(
            objectMapper,
            new TestCaseExecutionService(objectMapper));

    @Test
    void parse_accepts_valid_problem_response() {
        List<AiGeneratedProblemParser.GeneratedProblem> problems = parser.parse(validContent(scannerStarterCode(), validTestCases()), beginnerScope(), 1);

        assertEquals(1, problems.size());
        assertEquals("1부터 n까지 합을 구하세요.", problems.get(0).prompt());
        assertEquals("[{\"input\":\"3\",\"expectedOutput\":\"6\"}]", problems.get(0).testCaseSpec());
    }

    @Test
    void parse_rejects_empty_test_cases() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> parser.parse(validContent(scannerStarterCode(), "[]"), beginnerScope(), 1));

        assertEquals(ErrorCode.COMMON_INTERNAL_ERROR, exception.errorCode());
        assertEquals("AI가 생성한 테스트케이스가 비어 있습니다.", exception.getMessage());
    }

    @Test
    void parse_rejects_missing_expected_output() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> parser.parse(validContent(scannerStarterCode(), "[{\"input\":\"3\"}]"), beginnerScope(), 1));

        assertEquals(ErrorCode.COMMON_INTERNAL_ERROR, exception.errorCode());
        assertEquals("AI가 생성한 테스트케이스 형식이 올바르지 않습니다.", exception.getMessage());
    }

    @Test
    void parse_rejects_starter_code_outside_learning_scope() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> parser.parse(validContent(bufferedReaderStarterCode(), validTestCases()), beginnerScope(), 1));

        assertEquals(ErrorCode.COMMON_INTERNAL_ERROR, exception.errorCode());
        assertEquals("학습 범위에 없는 starterCode API가 포함되어 있습니다.", exception.getMessage());
    }

    @Test
    void parse_allows_restricted_api_when_learning_scope_contains_it() {
        List<AiGeneratedProblemParser.GeneratedProblem> problems = parser.parse(validContent(bufferedReaderStarterCode(), validTestCases()), bufferedReaderScope(), 1);

        assertEquals(1, problems.size());
    }

    @Test
    void parse_converts_literal_newline_sequences_in_prompt_before_persisting() {
        List<AiGeneratedProblemParser.GeneratedProblem> problems = parser.parse(
                validContentWithPrompt("첫 번째 문장\\n\\n두 번째 문장", scannerStarterCode(), validTestCases()),
                beginnerScope(),
                1);

        assertEquals("첫 번째 문장\n\n두 번째 문장", problems.get(0).prompt());
    }

    @Test
    void parse_rejects_solution_code_that_does_not_pass_test_cases() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> parser.parse(validContentWithSolution(
                        scannerStarterCode(),
                        "public class Solution { public static void main(String[] args) { System.out.println(0); } }",
                        validTestCases()),
                        beginnerScope(),
                        1));

        assertEquals(ErrorCode.COMMON_INTERNAL_ERROR, exception.errorCode());
        assertEquals("AI가 생성한 solutionCode가 테스트케이스를 통과하지 못했습니다.", exception.getMessage());
    }

    private String validContent(String starterCode, String testCases) {
        return validContent("1부터 n까지 합을 구하세요.", starterCode, solutionCode(), testCases);
    }

    private String validContentWithPrompt(String prompt, String starterCode, String testCases) {
        return validContent(prompt, starterCode, solutionCode(), testCases);
    }

    private String validContentWithSolution(String starterCode, String solutionCode, String testCases) {
        return validContent("1부터 n까지 합을 구하세요.", starterCode, solutionCode, testCases);
    }

    private String validContent(String prompt, String starterCode, String solutionCode, String testCases) {
        return """
                {
                  "problems": [
                    {
                      "prompt": "%s",
                      "starterCode": "%s",
                      "solutionCode": "%s",
                      "testCases": %s
                    }
                  ]
                }
                """.formatted(prompt, starterCode, solutionCode, testCases);
    }

    private String validTestCases() {
        return "[{\"input\":\"3\",\"expectedOutput\":\"6\"}]";
    }

    private String scannerStarterCode() {
        return "import java.util.Scanner; public class Solution { public static void main(String[] args) { Scanner scanner = new Scanner(System.in); } }";
    }

    private String solutionCode() {
        return "import java.util.Scanner; public class Solution { public static void main(String[] args) { Scanner scanner = new Scanner(System.in); int n = scanner.nextInt(); int sum = 0; for (int value = 1; value <= n; value++) { sum += value; } System.out.println(sum); } }";
    }

    private String bufferedReaderStarterCode() {
        return "import java.io.BufferedReader; public class Solution { public static void main(String[] args) { BufferedReader br; } }";
    }

    private ExamLearningScope beginnerScope() {
        return new ExamLearningScope(
                List.of(new LearnedItem("LESSON", "Java 변수", "변수", "int 변수를 배웁니다.", "int n = 1;")),
                List.of("public class Solution", "main method", "java.util.Scanner starter input"),
                "starterCode는 java.util.Scanner 기반으로 작성합니다. BufferedReader는 사용하지 않습니다.");
    }

    private ExamLearningScope bufferedReaderScope() {
        return new ExamLearningScope(
                List.of(new LearnedItem("LESSON", "BufferedReader", "입력", "BufferedReader 입력을 배웁니다.", "BufferedReader br;")),
                List.of("public class Solution", "main method", "BufferedReader"),
                "starterCode는 학습한 입력 방식을 사용합니다.");
    }
}
