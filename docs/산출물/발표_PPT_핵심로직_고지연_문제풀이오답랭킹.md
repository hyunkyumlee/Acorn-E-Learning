# Knowva 발표 PPT 핵심 로직 - 고지연 (문제풀이 / 오답복습 / 랭킹)

> 기준: 각 `##`은 PPT 1장 또는 1개 화면과 연결됩니다.  
> 화면만 보여주는 것이 아니라, **사용자 행동 -> 데이터 저장 -> 다음 흐름 결정**이 보이는 로직 위주로 정리했습니다.

---

## 1. 문제풀이 답안을 채점하고, 정답/오답/건너뛰기를 각각 다른 데이터 흐름으로 저장

- **구현 파일**: `ELearning/src/main/java/com/acorn/elearning/practice/service/PracticeService.java`
- **전달 가치**: 문제풀이 화면은 단순 제출이 아니라, 답안 채점 결과에 따라 점수, 오답노트, 건너뛰기 상태가 각각 다르게 저장되도록 설계했다.

### 전달 설명

- 사용자가 제출한 10문제 답안을 순회하면서 각 문제별로 정답 여부를 판정한다.
- `__SKIPPED__` 값을 통해 건너뛴 문제를 별도로 구분하고, 정답 채점 대상에서 제외한다.
- 정답이면 제출 이력 저장 후 점수를 지급한다.
- 오답이면 제출 이력 저장 후 오답노트 데이터로 기록한다.
- 이렇게 저장된 결과는 이후 결과 화면, 오답 복습, 랭킹 집계까지 이어진다.

### PPT 코드 발췌

```java
for (PracticeAnswerForm.SingleAnswer answerForm : answerList) {
    PracticeProblem problem = problemService.getProblem(answerForm.getProblemId());

    boolean isSkipped = "__SKIPPED__".equals(answerForm.getSubmittedAnswer());
    boolean isCorrect = !isSkipped && normalizeAnswer(problem.getAnswerText())
            .equals(normalizeAnswer(answerForm.getSubmittedAnswer()));

    PracticeSubmission submission = new PracticeSubmission();
    submission.setSetAttemptId(setAttemptId);
    submission.setSetItemId(setItem.getSetItemId());
    submission.setUserId(user.userId());
    submission.setProblemId(answerForm.getProblemId());
    submission.setSubmissionContext("PRACTICE_SET");
    submission.setSubmittedAnswer(isSkipped ? null : answerForm.getSubmittedAnswer());
    submission.setIsCorrect(isCorrect);
    submission.setIsSkipped(isSkipped);

    practiceSubmissionMapper.insertSubmission(submission);

    if (isSkipped) {
        continue;
    }

    if (isCorrect) {
        correctCount++;
        scoreService.giveScore(...);
    } else {
        wrongAnswerService.recordWrongAnswer(...);
    }
}
```

### 발표 포인트

> 이 로직의 핵심은 “답안을 받는다”에서 끝나지 않고, 정답은 점수로, 오답은 오답노트로, 건너뛰기는 별도 상태로 분기해서 이후 기능과 연결되도록 만든 점입니다.

---

## 2. 문제풀이 완료 후 통과 여부에 따라 학습 진행도와 다음 이동 경로를 분기

- **구현 파일**: `ELearning/src/main/java/com/acorn/elearning/practice/service/PracticeService.java`
- **전달 가치**: 문제풀이 결과는 단순 점수 계산이 아니라, 학습 진행도 업데이트와 다음 레슨 이동 흐름까지 함께 결정한다.

### 전달 설명

- 문제풀이 세트가 완료되면 상태를 `COMPLETED`로 바꾼다.
- 통과한 경우에는 레슨 문제풀이 통과, 출석, 세트 통과 점수까지 함께 반영한다.
- 실패한 경우에는 현재 레슨 기준 다시 풀기 경로를 만든다.
- 성공한 경우에는 같은 node 안에서 다음 lesson을 찾아 다음 학습 경로를 만든다.
- 즉, 문제풀이 결과가 학습 흐름 자체를 바꾸는 구조다.

### PPT 코드 발췌

```java
if (Boolean.TRUE.equals(attempt.getPassed())) {
    progressService.markPracticePassed(
            attempt.getUserId(),
            attempt.getSubjectId(),
            attempt.getNodeId()
    );

    userLessonProgressMapper.upsertPracticePassed(
            attempt.getUserId(),
            attempt.getLessonId()
    );

    attendanceService.recordAttendanceOnPracticePass(
            attempt.getUserId(),
            attempt.getSetAttemptId()
    );

    scoreService.giveScore(
            attempt.getUserId(),
            attempt.getSubjectId(),
            attempt.getSetAttemptId(),
            "PRACTICE_SET",
            50,
            "PRACTICE_SET_PASS",
            "PRACTICE_SET_PASS:" + attempt.getSetAttemptId()
    );
}
```

```java
if (!Boolean.TRUE.equals(attempt.getPassed())) {
    primaryPath = "/learning/practice?nodeId=" + attempt.getNodeId()
            + "&lessonId=" + attempt.getLessonId();
    primaryLabel = "문제 다시풀기";
} else {
    if (nextLessonId != null) {
        primaryPath = "/learning/lessons/" + nextLessonId;
        primaryLabel = "다음 레슨으로";
    } else {
        primaryPath = "/learning";
        primaryLabel = "학습 메인으로";
    }
}
```

### 발표 포인트

> 이 화면은 결과만 보여주는 것이 아니라, 문제풀이 결과를 학습 진행도와 연결하고 다음 학습 경로까지 자동으로 이어주는 역할을 합니다.

---

## 3. 오답 상세 화면에서 정답, 해설, 학습 위치 정보를 함께 조합해 복습 화면 구성

- **구현 파일**: `ELearning/src/main/java/com/acorn/elearning/practice/service/WrongAnswerService.java`
- **전달 가치**: 오답노트는 단순히 틀린 문제 목록만 보여주는 것이 아니라, 다시 학습할 수 있는 맥락 정보를 함께 제공한다.

### 전달 설명

- 사용자의 오답 기록을 먼저 확인해 본인 데이터만 접근 가능하게 한다.
- 오답이 연결된 원본 문제를 다시 조회한다.
- 문제 내용, 정답, 해설, lessonId, nodeId, 오답 횟수, 상태를 하나의 화면용 View로 묶는다.
- 이를 통해 오답 상세 화면은 “틀린 문제 재확인 + 해설 복습 + 현재 위치 파악”이 가능한 구조가 된다.

### PPT 코드 발췌

```java
WrongAnswer wrongAnswer = getOwnedWrongAnswer(sessionUser, wrongAnswerId);

PracticeProblem problem = practiceProblemMapper.findById(wrongAnswer.getProblemId())
        .orElseThrow(() -> new BusinessException(
                ErrorCode.COMMON_NOT_FOUND,
                "문제를 찾을 수 없습니다."
        ));

return WrongAnswerDetailView.from(
        wrongAnswer.getWrongAnswerId(),
        problem.getProblemId(),
        problem.getQuestion(),
        problem.getAnswerText(),
        problem.getExplanation(),
        problem.getLessonId(),
        problem.getNodeId(),
        wrongAnswer.getWrongCount(),
        wrongAnswer.getReviewStatus(),
        wrongAnswer.getRetryBonusAwarded()
);
```

### 발표 포인트

> 오답 화면은 단순 목록이 아니라, “왜 틀렸는지 다시 학습할 수 있는 화면”이 되도록 정답과 해설, 학습 위치 정보까지 함께 구성했습니다.

---

## 4. 오답 다시풀기에서 정답 처리와 +5 보상을 연결하고, 중복 지급은 막도록 설계

- **구현 파일**: `ELearning/src/main/java/com/acorn/elearning/practice/service/WrongAnswerService.java`
- **전달 가치**: 오답 복습은 다시 풀기만 하는 기능이 아니라, 상태 갱신과 보상 로직까지 연결되는 재도전 흐름이다.

### 전달 설명

- 사용자가 오답 문제를 다시 제출하면 기존 제출 이력이 있는지 먼저 확인한다.
- 기존 이력이 있으면 수정하고, 없으면 새 제출을 생성한다.
- 정답일 경우 오답 상태를 `SOLVED`로 바꾸고 재정답 보상 지급 여부를 저장한다.
- 이때 `idempotency key`를 사용해 +5 점수가 중복 지급되지 않도록 방지한다.
- 즉, 오답 복습은 학습 이력과 보상 시스템이 함께 연결된 기능이다.

### PPT 코드 발췌

```java
boolean isCorrect = normalizeAnswer(problem.getAnswerText())
        .equals(normalizeAnswer(form.getSubmittedAnswer()));

Optional<PracticeSubmission> existingSubmission =
        practiceSubmissionMapper.findBySetAttemptIdAndProblemIdAndContext(
                wrongAnswer.getSetAttemptId(),
                problem.getProblemId(),
                "WRONG_ANSWER_RETRY"
        );

if (existingSubmission.isPresent()) {
    PracticeSubmission submission = existingSubmission.get();
    submission.setSubmittedAnswer(form.getSubmittedAnswer());
    submission.setIsCorrect(isCorrect);
    practiceSubmissionMapper.updateSubmission(submission);
} else {
    PracticeSubmission submission = new PracticeSubmission();
    submission.setSetAttemptId(wrongAnswer.getSetAttemptId());
    submission.setUserId(sessionUser.userId());
    submission.setProblemId(problem.getProblemId());
    submission.setSubmissionContext("WRONG_ANSWER_RETRY");
    submission.setSubmittedAnswer(form.getSubmittedAnswer());
    submission.setIsCorrect(isCorrect);
    practiceSubmissionMapper.insertSubmission(submission);
}
```

```java
if (isCorrect) {
    String retryIdempotencyKey = "WRONG_ANSWER_RETRY:" + wrongAnswer.getWrongAnswerId();

    wrongAnswer.setReviewStatus("SOLVED");
    wrongAnswer.setRetryBonusAwarded(true);
    wrongAnswer.setLastSubmissionId(latestSubmissionId);

    int existingScoreEventCount = scoreEventMapper.countByIdempotencyKey(retryIdempotencyKey);

    if (existingScoreEventCount == 0) {
        scoreService.giveScore(
                sessionUser.userId(),
                problem.getSubjectId(),
                wrongAnswer.getWrongAnswerId(),
                "WRONG_ANSWER_RETRY",
                5,
                "WRONG_ANSWER_RETRY_CORRECT",
                retryIdempotencyKey
        );
    }
}
```

### 발표 포인트

> 이 로직은 오답을 다시 맞혔을 때 단순 정답 처리로 끝나는 것이 아니라, 상태를 갱신하고 보상까지 연결하되 중복 지급은 막도록 만든 점이 핵심입니다.

---

## 5. 랭킹 화면은 단순 조회가 아니라 기간, 과목, 리그에 맞춰 데이터를 다시 조합해서 생성

- **구현 파일**: `ELearning/src/main/java/com/acorn/elearning/ranking/service/RankingService.java`
- **전달 가치**: 랭킹 화면은 조회 결과를 그대로 보여주는 것이 아니라, 기간별/과목별/리그별 조건을 반영해 화면 데이터를 새로 조합한다.

### 전달 설명

- 과목 선택 여부에 따라 주간 / 월간 기준을 다르게 적용한다.
- 조회 전에 ranking score를 최신 기준으로 갱신한다.
- 현재 사용자의 리그 코드를 읽어 해당 리그 기준으로 랭킹을 필터링한다.
- top3, 4위 이하 목록, 내 순위 요약, 점수 구성 데이터를 각각 만든다.
- 즉, 랭킹 화면은 하나의 테이블 조회가 아니라 여러 집계 결과를 조합한 화면이다.

### PPT 코드 발췌

```java
private Map<String, Object> buildRankingData(SessionUser sessionUser, Long subjectId) {
    String effectivePeriodType = resolvePeriodType(subjectId);

    refreshRankingScores(subjectId, effectivePeriodType);

    String leagueCode = resolveCurrentLeagueCode(sessionUser.userId());

    List<Map<String, Object>> filteredScores =
            subjectId == null
                    ? rankingScoreMapper.findMonthlyGlobalRankingFromSubjects(
                    currentMonthlyPeriodKey(),
                    leagueCode
            )
                    : rankingScoreMapper.findWeeklySubjectRanking(
                    subjectId,
                    currentWeeklyPeriodKey(),
                    leagueCode
            );
```

```java
List<Map<String, Object>> top3 = new ArrayList<>();
for (int i = 0; i < Math.min(3, filteredScores.size()); i++) {
    Map<String, Object> row = filteredScores.get(i);

    Map<String, Object> item = new HashMap<>();
    item.put("rankNo", row.get("rankNo"));
    item.put("userId", row.get("userId"));
    item.put("nickname", row.get("nickname"));
    item.put("score", row.get("score"));
    top3.add(item);
}
```

```java
Map<String, Object> scoreBreakdown = buildScoreBreakdown(
        sessionUser.userId(),
        subjectId,
        effectivePeriodType
);
```

### 발표 포인트

> 랭킹은 단순히 점수순으로 보여주는 기능이 아니라, 과목과 리그, 기간 조건을 반영해 사용자의 현재 위치와 점수 구성을 한 번에 보여주는 방식으로 구현했습니다.

---

## 6. 점수구성은 문제풀이 / 시험 / 일일학습 점수를 분리해 사용자가 성장 근거를 볼 수 있게 설계

- **구현 파일**: `ELearning/src/main/java/com/acorn/elearning/ranking/service/RankingService.java`
- **전달 가치**: 총점만 보여주는 것이 아니라, 어떤 활동으로 점수가 쌓였는지 분해해서 보여줌으로써 성취 근거를 시각화했다.

### 전달 설명

- 랭킹 화면의 점수구성은 단일 총점이 아니라 `practiceScore`, `examScore`, `dailyScore`로 나눈다.
- 주간 화면이면 주간 기준 점수만, 월간 화면이면 월간 기준 점수만 합산한다.
- 이를 통해 사용자는 단순히 “몇 점인지”가 아니라 “어떤 활동으로 점수를 얻었는지” 확인할 수 있다.

### PPT 코드 발췌

```java
private Map<String, Object> buildScoreBreakdown(Long userId, Long subjectId, String periodType) {
    Map<String, Object> scoreBreakdown = new HashMap<>();

    if (PERIOD_WEEKLY.equals(periodType)) {
        scoreBreakdown.put(
                "practiceScore",
                safeScore(rankingScoreMapper.sumWeeklyPracticeScore(userId, subjectId))
        );
        scoreBreakdown.put(
                "examScore",
                safeScore(rankingScoreMapper.sumWeeklyExamScore(userId, subjectId))
        );
        scoreBreakdown.put(
                "dailyScore",
                safeScore(rankingScoreMapper.sumWeeklyDailyScore(userId, subjectId))
        );
    } else {
        scoreBreakdown.put(
                "practiceScore",
                safeScore(rankingScoreMapper.sumMonthlyPracticeScore(userId, subjectId))
        );
        scoreBreakdown.put(
                "examScore",
                safeScore(rankingScoreMapper.sumMonthlyExamScore(userId, subjectId))
        );
        scoreBreakdown.put(
                "dailyScore",
                safeScore(rankingScoreMapper.sumMonthlyDailyScore(userId, subjectId))
        );
    }

    return scoreBreakdown;
}
```

### 발표 포인트

> 총점을 하나로만 보여주면 사용자는 왜 그 점수가 나왔는지 알기 어렵습니다. 그래서 문제풀이, 시험, 일일학습 점수를 분리해 성장 근거가 보이도록 설계했습니다.
