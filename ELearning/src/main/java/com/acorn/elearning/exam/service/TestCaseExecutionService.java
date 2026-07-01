package com.acorn.elearning.exam.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.exam.dto.response.TestCaseExecutionResult;
import com.acorn.elearning.exam.model.AiExamProblem;
import com.acorn.elearning.exam.model.ExamAnswer;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class TestCaseExecutionService {
    private static final Duration COMPILE_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration RUN_TIMEOUT = Duration.ofSeconds(3);

    private final ObjectMapper objectMapper;

    public TestCaseExecutionService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public TestCaseExecutionResult execute(AiExamProblem problem, ExamAnswer answer) {
        List<TestCase> cases = parseCases(problem.getTestCaseSpec());
        Path workDir = createWorkDir();
        try {
            Files.writeString(workDir.resolve("Solution.java"), answer.getAnswerText(), StandardCharsets.UTF_8);
            ProcessResult compile = runProcess(workDir, COMPILE_TIMEOUT, "javac", "Solution.java");
            if (compile.exitCode() != 0) {
                return compileError(cases, compile.output());
            }
            return runCases(workDir, cases);
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR, "테스트케이스 실행 파일을 만들 수 없습니다.");
        } finally {
            deleteWorkDir(workDir);
        }
    }

    private List<TestCase> parseCases(String testCaseSpec) {
        try {
            JsonNode root = objectMapper.readTree(testCaseSpec);
            JsonNode caseNodes = root.isArray() ? root : root.path("cases");
            if (!caseNodes.isArray() || caseNodes.isEmpty()) {
                throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "테스트케이스가 필요합니다.");
            }
            List<TestCase> cases = new ArrayList<>();
            for (JsonNode node : caseNodes) {
                cases.add(new TestCase(node.path("input").asText(), node.path("expectedOutput").asText()));
            }
            return cases;
        } catch (JacksonException exception) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "테스트케이스 형식이 올바르지 않습니다.");
        }
    }

    private TestCaseExecutionResult runCases(Path workDir, List<TestCase> cases) {
        List<TestCaseExecutionResult.CaseResult> results = new ArrayList<>();
        for (TestCase testCase : cases) {
            ProcessResult result = runProcessWithInput(workDir, testCase.input(), RUN_TIMEOUT, "java", "-cp", workDir.toString(), "Solution");
            String actualOutput = normalize(result.output());
            String expectedOutput = normalize(testCase.expectedOutput());
            boolean passed = result.exitCode() == 0 && expectedOutput.equals(actualOutput);
            results.add(new TestCaseExecutionResult.CaseResult(
                    testCase.input(),
                    testCase.expectedOutput(),
                    actualOutput,
                    passed,
                    result.exitCode() == 0 ? null : result.output()));
        }
        int passedCount = (int) results.stream().filter(TestCaseExecutionResult.CaseResult::passed).count();
        return new TestCaseExecutionResult(
                passedCount == cases.size() ? "PASSED" : "FAILED",
                passedCount == cases.size(),
                passedCount,
                cases.size(),
                results);
    }

    private TestCaseExecutionResult compileError(List<TestCase> cases, String output) {
        List<TestCaseExecutionResult.CaseResult> results = cases.stream()
                .map(testCase -> new TestCaseExecutionResult.CaseResult(
                        testCase.input(),
                        testCase.expectedOutput(),
                        "",
                        false,
                        output))
                .toList();
        return new TestCaseExecutionResult("COMPILE_ERROR", false, 0, cases.size(), results);
    }

    private ProcessResult runProcess(Path workDir, Duration timeout, String... command) {
        return runProcessWithInput(workDir, "", timeout, command);
    }

    private ProcessResult runProcessWithInput(Path workDir, String input, Duration timeout, String... command) {
        try {
            Process process = new ProcessBuilder(command)
                    .directory(workDir.toFile())
                    .redirectErrorStream(true)
                    .start();
            CompletableFuture<String> output = CompletableFuture.supplyAsync(() -> readOutput(process));
            process.getOutputStream().write(input.getBytes(StandardCharsets.UTF_8));
            process.getOutputStream().close();
            boolean completed = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!completed) {
                process.destroyForcibly();
                return new ProcessResult(-1, "실행 시간이 초과되었습니다.");
            }
            return new ProcessResult(process.exitValue(), output.join());
        } catch (IOException exception) {
            return new ProcessResult(-1, "코드 실행 도구를 사용할 수 없습니다.");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return new ProcessResult(-1, "코드 실행이 중단되었습니다.");
        }
    }

    private String readOutput(Process process) {
        try {
            return new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            return "실행 결과를 읽을 수 없습니다.";
        }
    }

    private Path createWorkDir() {
        try {
            return Files.createTempDirectory("knowva-judge-");
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR, "테스트케이스 실행 공간을 만들 수 없습니다.");
        }
    }

    private void deleteWorkDir(Path workDir) {
        try {
            Files.walk(workDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> path.toFile().delete());
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR, "테스트케이스 실행 공간을 정리할 수 없습니다.");
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.strip().replace("\r\n", "\n");
    }

    private record TestCase(String input, String expectedOutput) {}
    private record ProcessResult(int exitCode, String output) {}
}
