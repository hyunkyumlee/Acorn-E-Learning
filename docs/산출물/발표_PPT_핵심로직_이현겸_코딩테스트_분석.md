# Knowva 발표 PPT용 핵심 로직 — 이현겸 (코딩테스트 · AI 분석)

> 기준: 각 `##`는 PPT 한 장이다. AI가 문제를 만드는 지점부터 채점, 레벨 해금, 누적 분석까지의 **상태 전이와 검증 흐름**만 남겼다.

## 1. 완료한 레슨 범위 안에서만 출제하고, AI 정답을 실행 검증

- **구현 파일**: `ELearning/src/main/java/com/acorn/elearning/exam/service/ExamLearningScopeService.java`, `AiGeneratedProblemParser.java`
- **핵심 가치**: “난이도에 맞는 문제”가 아니라 학습자가 실제로 끝낸 레슨·개념 안에서만 문제를 만든다.

### 핵심 설명

- 과목·난이도별 필수 레슨의 이론 학습과 문제풀이를 모두 완료한 내역만 출제 scope로 수집한다.
- scope에서 허용 개념을 만들고, AI 정답 코드에 scope 밖 API가 있으면 거절한다.
- AI 응답에 테스트케이스가 있는지, `Solution` class와 완성된 정답 코드가 있는지 검증한다.
- 형식 검증만 하지 않고 정답 코드를 실제 test-case runner로 실행해 통과한 문제만 DB에 저장한다.

### PPT 코드 발췌

```java
List<LearnedItem> learnedItems = examLearningScopeMapper
        .findCompletedLessonScope(userId, subjectId, levelCode)
        .stream()
        .map(this::toLearnedItem)
        .toList();

if (learnedItems.isEmpty()) {
    throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED,
            "완료한 필수 레슨 범위가 없어 AI 시험 문제를 생성할 수 없습니다.");
}

return new ExamLearningScope(
        learnedItems,
        allowedConcepts(learnedItems),
        """
        starterCode는 java.util.Scanner 기반으로 작성합니다.
        사용자는 TODO 주석 아래의 풀이 로직과 출력만 작성합니다.
        """);
```

```java
String solutionCode = requiredText(problem, "solutionCode");
validateSolutionCode(solutionCode, learningScopeText);

String testCaseSpec = objectMapper.writeValueAsString(validTestCases(problem));
validateSolutionExecution(solutionCode, testCaseSpec);

TestCaseExecutionResult result = testCaseExecutionService.execute(problem, answer);
if (!result.passed()) {
    throw invalidGeneratedProblem(
            "AI가 생성한 solutionCode가 테스트케이스를 통과하지 못했습니다.");
}
```

### 발표 포인트

> “AI에게 범위만 설명하는 수준이 아니다. 학습 데이터로 출제 범위를 제한하고, AI가 준 정답도 우리의 채점 엔진에서 다시 실행해 검증한다.”

---

## 2. 시험 생성 상태 전이와 잘못된 AI 응답의 재시도 기록

- **구현 파일**: `ELearning/src/main/java/com/acorn/elearning/exam/service/AiExamService.java`, `AiRequestLogService.java`, `ExamSessionStatusPolicy.java`
- **핵심 가치**: 더블 클릭으로 시험을 중복 생성하지 않고, AI 생성 실패를 `FAILED` 상태와 요청 로그로 남긴다.

### 핵심 설명

- 이미 진행 중인 같은 과목·난이도 시험이 있으면 새 시험을 만들지 않고 기존 시험을 반환한다.
- DB unique key 충돌이 나도 잠금 조회로 현재 active session을 다시 찾아 같은 시험으로 수렴한다.
- 시험은 `CREATED → READY`로 전이하고, 문제 생성 중 오류가 나면 `FAILED`로 명확히 남긴다.
- AI가 반환한 문제가 검증에 실패하면 최대 2회까지 재생성하며, 요청·응답·실패 사유를 독립 transaction으로 기록한다.

### PPT 코드 발췌

```java
ExamSession activeSession = examSessionMapper
        .findLatestActiveByUserSubjectLevel(
                userId, request.subjectId(), request.levelCode())
        .orElse(null);
if (activeSession != null) {
    return detail(userId, activeSession.getExamId());
}

session.setStatus(ExamSessionStatusPolicy.CREATED);
try {
    examSessionMapper.insert(session);
} catch (DuplicateKeyException exception) {
    return examSessionMapper
            .findLatestActiveByUserSubjectLevelForUpdate(
                    userId, request.subjectId(), request.levelCode())
            .map(active -> detailForUpdate(userId, active.getExamId()))
            .orElseThrow(() -> exception);
}

try {
    generateProblems(session.getExamId(), request, learningScope);
} catch (RuntimeException exception) {
    session.setStatus(ExamSessionStatusPolicy.FAILED);
    examSessionMapper.updateStatus(session);
    throw exception;
}
session.setStatus(ExamSessionStatusPolicy.READY);
examSessionMapper.updateStatus(session);
```

```java
for (int retryNo = 0; retryNo < PROBLEM_GENERATION_MAX_ATTEMPTS; retryNo++) {
    AiRequestLog log = aiRequestLogService.start(
            TARGET_TYPE, examId, "PROBLEM_GENERATION", chatGptRequest);
    ChatGptResponse response = null;
    try {
        response = chatGptApiClient.send(chatGptRequest);
        saveGeneratedProblems(examId, response.content(), learningScope);
        aiRequestLogService.success(log, response);
        return;
    } catch (InvalidGeneratedProblemException exception) {
        aiRequestLogService.failed(log, response, exception);
    }
}
```

### 발표 포인트

> “AI 생성은 성공만 가정하지 않았다. 시험 자체의 상태와 AI 요청 log를 분리해, 중복 생성·형식 불량·재시도 이력을 모두 추적할 수 있다.”

---

## 3. 3문제 채점 결과를 저장하고, 2문제 이상 통과 시 다음 레벨 해금

- **구현 파일**: `ELearning/src/main/java/com/acorn/elearning/exam/service/AiExamService.java`, `TestCaseExecutionService.java`
- **핵심 가치**: 단순 정오답 표시가 아니라 테스트케이스 결과, AI 리뷰, 시험 결과, 다음 레벨 해금을 하나의 transaction으로 연결한다.

### 핵심 설명

- 제출 전 3문제의 답안이 모두 존재하는지 확인한다.
- 각 답안은 서버 test-case runner로 컴파일·실행하며, 통과 개수·정오답·실행 결과를 저장한다.
- 문제별 AI 리뷰도 같은 답안 row에 기록해 결과 화면과 분석 화면의 근거가 된다.
- 3문제 중 2문제 이상 통과하면 시험을 `PASSED`로 확정하고 다음 레벨을 해금한다.

### PPT 코드 발췌

```java
if (problems.size() < PROBLEM_COUNT || answers.size() < PROBLEM_COUNT) {
    throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED,
            "모든 문제의 답안을 제출해야 합니다.");
}

for (AiExamProblem problem : problems) {
    ExamAnswer answer = answers.get(problem.getAiProblemId());
    TestCaseExecutionResult result = testCaseExecutionService.execute(problem, answer);

    answer.setPassedCaseCount(result.passedCount());
    answer.setIsCorrect(result.passed());
    answer.setTestCaseResult(toJson(result));
    answer.setAiReview(aiReviewService.reviewAnswer(
            answer.getAnswerId(), problem.getPrompt(),
            ExamStarterCodeResolver.starterCode(problem),
            answer.getAnswerText(), result));
    examAnswerMapper.updateGradingResult(answer);

    if (result.passed()) {
        correctCount++;
    }
}

session.setResultStatus(correctCount >= PASS_COUNT ? "PASSED" : "FAILED");
if (correctCount >= PASS_COUNT) {
    unlockService.unlockNextLevel(
            session.getUserId(), session.getSubjectId(),
            session.getLevelCode(), session.getExamId());
}
```

### 발표 포인트

> “시험 제출은 점수 계산에서 끝나지 않는다. 실행 결과와 AI 피드백을 이후 분석의 원본 데이터로 저장하고, 합격 여부가 곧 다음 학습 난이도 해금으로 이어진다.”

---

## 4. AI 분석 리포트는 한 번만 만들고, 학습자 작성 코드만 근거로 사용

- **구현 파일**: `ELearning/src/main/java/com/acorn/elearning/analysis/service/AiAnalysisService.java`, `exam/support/ExamStarterCodeResolver.java`
- **핵심 가치**: 중복 클릭·자동 새로고침에도 LLM 분석을 중복 생성하지 않고, 시스템 기본 코드가 학습자 실력으로 분석되는 오류를 막는다.

### 핵심 설명

- 사용자·시험 단위 lock과 DB의 `PENDING` 선점 row를 함께 사용한다.
- 이미 생성됐거나 다른 요청이 선점한 report는 재생성하지 않고 기존 결과를 돌려준다.
- `DuplicateKeyException` 상황에서는 잠금 조회로 기존 report를 회수한다.
- 분석 payload에 들어가는 starter code를 서버 공통 TODO 골격으로 통일해, AI raw 정답 로직이 섞여도 분석 AI에는 전달되지 않게 한다.

### PPT 코드 발췌

```java
synchronized (reportLock(userId, session.getExamId())) {
    AiAnalysisReport existing = aiAnalysisReportMapper
            .findByExamIdAndUserId(session.getExamId(), userId)
            .orElse(null);
    if (existing != null) {
        return responseFor(userId, existing);
    }

    ReportClaim claim = pendingReport(userId, session);
    if (!claim.created()) {
        return responseFor(userId, claim.report());
    }
    generateContent(claim.report(), session);
    return responseFor(userId, claim.report());
}
```

```java
private ReportClaim pendingReport(Long userId, ExamSession session) {
    AiAnalysisReport report = new AiAnalysisReport();
    report.setUserId(userId);
    report.setExamId(session.getExamId());
    report.setStatus("PENDING");
    report.setRetryCount(0);
    try {
        aiAnalysisReportMapper.insert(report);
        return new ReportClaim(report, true);
    } catch (DuplicateKeyException exception) {
        AiAnalysisReport existing = aiAnalysisReportMapper
                .findByExamIdAndUserIdForUpdate(session.getExamId(), userId)
                .orElseThrow(() -> exception);
        return new ReportClaim(existing, false);
    }
}

summaries.forEach(summary ->
        summary.setStarterCode(ExamStarterCodeResolver.defaultStarterCode()));
```

### 발표 포인트

> “AI 분석에서 중요한 것은 생성 모델보다 입력 데이터의 신뢰성이다. 중복 호출을 막고, 기본 코드와 제출 코드를 분리해 학습자가 직접 작성한 로직만 분석 근거로 사용한다.”
