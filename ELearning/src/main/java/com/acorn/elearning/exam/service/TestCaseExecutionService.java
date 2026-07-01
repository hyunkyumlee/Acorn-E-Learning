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
import java.nio.file.attribute.PosixFilePermission;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private static final int MAX_CASE_COUNT = 20;
    private static final int MAX_CASE_TEXT_LENGTH = 4_000;
    private static final int MAX_OUTPUT_BYTES = 16_384;

    private final ObjectMapper objectMapper;
    private final CodeExecutionSecurityPolicy securityPolicy = new CodeExecutionSecurityPolicy();

    public TestCaseExecutionService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public TestCaseExecutionResult execute(AiExamProblem problem, ExamAnswer answer) {
        List<TestCase> cases = parseCases(problem.getTestCaseSpec());
        String violationMessage = securityPolicy.violationMessage(answer.getAnswerText());
        if (!violationMessage.isBlank()) {
            return securityViolation(cases, violationMessage);
        }
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
            if (!caseNodes.isArray() || caseNodes.size() == 0 || caseNodes.size() > MAX_CASE_COUNT) {
                throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "테스트케이스가 필요합니다.");
            }
            List<TestCase> cases = new ArrayList<>();
            for (JsonNode node : caseNodes) {
                cases.add(new TestCase(boundedText(node, "input"), boundedText(node, "expectedOutput")));
            }
            return cases;
        } catch (JacksonException exception) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "테스트케이스 형식이 올바르지 않습니다.");
        }
    }

    private String boundedText(JsonNode node, String fieldName) {
        String value = node.path(fieldName).asText();
        if (value.length() > MAX_CASE_TEXT_LENGTH) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "테스트케이스 크기가 허용 범위를 초과했습니다.");
        }
        return value;
    }

    private TestCaseExecutionResult runCases(Path workDir, List<TestCase> cases) {
        List<TestCaseExecutionResult.CaseResult> results = new ArrayList<>();
        for (TestCase testCase : cases) {
            ProcessResult result = runProcessWithInput(
                    workDir,
                    testCase.input(),
                    RUN_TIMEOUT,
                    "java",
                    "-Xms16m",
                    "-Xmx64m",
                    "-Dfile.encoding=UTF-8",
                    "-Duser.home=" + workDir,
                    "-Djava.io.tmpdir=" + workDir,
                    "-cp",
                    workDir.toString(),
                    "Solution");
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

    private TestCaseExecutionResult securityViolation(List<TestCase> cases, String message) {
        List<TestCaseExecutionResult.CaseResult> results = cases.stream()
                .map(testCase -> new TestCaseExecutionResult.CaseResult(
                        testCase.input(),
                        testCase.expectedOutput(),
                        "",
                        false,
                        message))
                .toList();
        return new TestCaseExecutionResult("SECURITY_VIOLATION", false, 0, cases.size(), results);
    }

    private ProcessResult runProcess(Path workDir, Duration timeout, String... command) {
        return runProcessWithInput(workDir, "", timeout, command);
    }

    private ProcessResult runProcessWithInput(Path workDir, String input, Duration timeout, String... command) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command)
                    .directory(workDir.toFile())
                    .redirectErrorStream(true);
            applyRestrictedEnvironment(processBuilder.environment());
            Process process = processBuilder.start();
            CompletableFuture<String> output = CompletableFuture.supplyAsync(() -> readOutput(process));
            process.getOutputStream().write(input.getBytes(StandardCharsets.UTF_8));
            process.getOutputStream().close();
            boolean completed = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!completed) {
                process.destroyForcibly();
                process.waitFor(1, TimeUnit.SECONDS);
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

    private void applyRestrictedEnvironment(Map<String, String> environment) {
        String path = environment.get("PATH");
        environment.clear();
        if (path != null) {
            environment.put("PATH", path);
        }
        environment.put("LANG", "C.UTF-8");
        environment.put("LC_ALL", "C.UTF-8");
    }

    private String readOutput(Process process) {
        try {
            byte[] output = process.getInputStream().readNBytes(MAX_OUTPUT_BYTES + 1);
            String value = new String(output, 0, Math.min(output.length, MAX_OUTPUT_BYTES), StandardCharsets.UTF_8);
            if (output.length > MAX_OUTPUT_BYTES) {
                return value + "\n출력 크기가 허용 범위를 초과하여 일부만 표시됩니다.";
            }
            return value;
        } catch (IOException exception) {
            return "실행 결과를 읽을 수 없습니다.";
        }
    }

    private Path createWorkDir() {
        try {
            Path workDir = Files.createTempDirectory("knowva-judge-");
            restrictWorkDirPermission(workDir);
            return workDir;
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.COMMON_INTERNAL_ERROR, "테스트케이스 실행 공간을 만들 수 없습니다.");
        }
    }

    private void restrictWorkDirPermission(Path workDir) throws IOException {
        try {
            Files.setPosixFilePermissions(
                    workDir,
                    Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE));
        } catch (UnsupportedOperationException ignored) {
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
