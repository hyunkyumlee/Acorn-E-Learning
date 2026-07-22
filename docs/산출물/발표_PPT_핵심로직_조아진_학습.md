# Knowva 발표 PPT용 핵심 로직 — 조아진 (학습 · 커리큘럼 · 레벨테스트 · 출석)

> 기준: 각 `##`는 PPT 한 장이다. 코드 블록은 실제 구현을 발표 분량으로만 발췌했다. 왼쪽에는 **핵심 설명**, 오른쪽에는 **코드**를 배치하면 된다.
> 슬라이드 순서는 학습자 동선(시작 → 레벨 판정 → 로드맵 → 레슨 → 해금 → 출석)을 그대로 따른다.

## 1. 학습 시작 분기: 레벨을 언제 여는지로 두 경로를 나눈다

- **구현 파일**: `ELearning/src/main/java/com/acorn/elearning/learning/controller/OnboardingController.java`, `service/EnrollmentService.java`
- **핵심 가치**: 온보딩의 마지막 선택 하나가 이후 모든 화면의 잠금 상태를 결정하므로, 수강 신청과 레벨 해금 시점을 명시적으로 분리했다.

### 핵심 설명

- "기초부터 시작"은 수강 신청과 동시에 최저 레벨(BRONZE)을 열고, "레벨 스캔"은 신청만 하고 레벨을 열지 않는다.
- 레벨 스캔 경로에서 레벨을 여는 주체는 온보딩이 아니라 레벨 테스트 채점 결과다.
- 문항이 등록되지 않은 과목은 레벨 테스트를 시작할 수 없으므로 기초 시작으로 자동 처리한다.
- 수강 신청은 `(user_id, subject_id)` 기준 멱등이라 뒤로 가기·재클릭에도 신청 행이 늘어나지 않는다.

### PPT 코드 발췌

```java
// 문항이 등록되지 않은 과목은 레벨 테스트로 시작할 수 없어 기초 시작으로 처리한다.
if ("SCAN".equals(startMode) && levelTestService.hasQuestions(subjectId)) {
    // 레벨 테스트로 시작: 신청만 하고 레벨은 열지 않는다(채점 결과가 판정 등급까지 연다).
    enrollmentService.enroll(user.userId(), subjectId, EnrollmentService.START_MODE_LEVEL_TEST);
    return "redirect:/learning/level-test?subjectId=" + subjectId;
}
// 기초부터 시작: 신청과 함께 최저 레벨을 연다.
enrollmentService.enroll(user.userId(), subjectId, EnrollmentService.START_MODE_BASIC);
profileWriteMapper.updateLevelIfHigher(user.userId(), DEFAULT_LEVEL_CODE);
```

```java
@Transactional
public UserSubjectEnrollment enroll(Long userId, Long subjectId, String startMode) {
    UserSubjectEnrollment existing =
            enrollmentMapper.findByUserAndSubject(userId, subjectId).orElse(null);
    if (existing == null) {
        existing = new UserSubjectEnrollment();
        existing.setUserId(userId);
        existing.setSubjectId(subjectId);
        existing.setStatus(STATUS_ACTIVE);
        existing.setStartMode(startMode);
        existing.setEnrolledAt(LocalDateTime.now());
        enrollmentMapper.insert(existing);
    }

    if (START_MODE_BASIC.equals(startMode)) {
        unlockService.unlock(userId, subjectId, LOWEST_LEVEL_CODE, SOURCE_ENROLLMENT, null);
    }
    return existing;
}
```

### 발표 포인트

> "학습을 시작하는 방법은 두 가지지만, 레벨을 여는 권한은 한 곳으로 모았다. 기초 시작만 즉시 해금하고, 레벨 스캔은 채점 결과만 해금할 수 있게 했다."

---

## 2. 레벨 테스트: 채점부터 해금까지 4개 테이블을 한 트랜잭션으로

- **구현 파일**: `ELearning/src/main/java/com/acorn/elearning/learning/service/LevelTestService.java`
- **핵심 가치**: 8문항 제출 한 번이 attempt · answers · 프로필 레벨 · 해금 이력을 동시에 바꾸므로, 부분 성공으로 인한 상태 불일치를 원천 차단한다.

### 핵심 설명

- 정답 판정 기준은 제출 폼이 아니라 DB의 활성 문항 집합이라, 화면으로 정답을 내려보내지 않고 조작도 불가능하다.
- attempt를 먼저 insert해 `useGeneratedKeys`로 PK를 확보하고, 이를 answers의 FK로 사용한다.
- 정답 개수로 등급을 산정한 뒤(0-2 Bronze / 3-5 Silver / 6-8 Gold) 프로필 레벨과 해금 이력에 반영한다.
- 레벨 테스트는 배치(placement) 성격이므로 판정 등급까지의 하위 레벨을 모두 연다 — 화면의 잠금 표시와 서버 진입 가드가 같은 테이블을 보게 만드는 조건이다.

### PPT 코드 발췌

```java
@Transactional
public LevelTestResultView submitAndApply(SessionUser user, LevelTestForm form) {
    // 채점 기준(정답)은 폼이 아니라 DB의 활성 문항 집합을 신뢰한다.
    List<LevelTestQuestion> questions = questionMapper.findActiveBySubjectId(subjectId);

    attemptMapper.insert(attempt); // useGeneratedKeys → attempt.attemptId 채워짐
    Long attemptId = attempt.getAttemptId();

    int correctCount = 0;
    for (LevelTestQuestion q : questions) {
        Long correctChoiceId = correctChoiceId(q.getQuestionId());
        Long submittedChoiceId = submitted.get(q.getQuestionId());
        boolean isCorrect = submittedChoiceId != null
                && submittedChoiceId.equals(correctChoiceId);
        if (isCorrect) {
            correctCount++;
        }
        answerMapper.insert(answer);
    }

    String resultLevel = grade(correctCount);
    attemptMapper.update(attempt);
    profileWriteMapper.updateLevelIfHigher(userId, resultLevel);

    // unlock 이력 반영(멱등). 배치(placement) 성격 → 판정 등급까지의 레벨을 모두 연다.
    for (String level : levelsUpTo(resultLevel)) {
        upsertUnlock(userId, subjectId, level);
    }
    return new LevelTestResultView(attemptId, subjectId, resultLevel, correctCount, totalCount);
}
```

```java
/** 낮은 레벨 → 판정 레벨(포함)까지의 레벨 코드 목록. BRONZE ≤ SILVER ≤ GOLD 순. */
private static List<String> levelsUpTo(String resultLevel) {
    if (LEVEL_GOLD.equals(resultLevel)) {
        return List.of(LEVEL_BRONZE, LEVEL_SILVER, LEVEL_GOLD);
    }
    if (LEVEL_SILVER.equals(resultLevel)) {
        return List.of(LEVEL_BRONZE, LEVEL_SILVER);
    }
    return List.of(LEVEL_BRONZE);
}

/** 정답 개수 → 등급. 0-2 Bronze / 3-5 Silver / 6-8 Gold. */
private static String grade(int correctCount) {
    if (correctCount <= 2) {
        return LEVEL_BRONZE;
    }
    if (correctCount <= 5) {
        return LEVEL_SILVER;
    }
    return LEVEL_GOLD;
}
```

### 발표 포인트

> "레벨 판정에 AI를 쓰지 않았다. 규칙이 명확한 영역은 규칙으로 처리하고, 대신 결과가 4개 테이블에 한 번에 반영되도록 트랜잭션 경계를 잡는 데 집중했다. 재응시로 등급이 낮게 나와도 이미 도달한 레벨은 내려가지 않는다."

---

## 3. 로드맵 행성 상태를 서버에서 한 번에 확정

- **구현 파일**: `ELearning/src/main/java/com/acorn/elearning/learning/controller/LearningController.java`
- **핵심 가치**: 화면에 보이는 잠금 상태와 서버의 진입 가드가 `user_level_unlocks` 하나를 보게 만들어, "보이는데 누르면 403"이 생기지 않게 한다.

### 핵심 설명

- 해금되지 않은 레벨은 다른 판정을 하기 전에 전 노드를 `locked`로 확정한다.
- 다음 레벨이 이미 열려 있으면 지나온 레벨로 보고 순차 잠금을 해제하되, 학습 이력이 없으므로 완료로 치지는 않는다.
- 행성은 완료 행성 수 기준으로 `done / current / locked`를 순차 판정해 다음 한 칸만 `current`가 된다.
- 판정 결과는 상태 문자열 map으로 템플릿에 넘기고, 템플릿은 같은 조건식을 다시 계산하지 않는다.

### PPT 코드 발췌

```java
// 열리지 않은 레벨은 노드를 하나도 들어갈 수 없다. 레슨 진입 가드도 user_level_unlocks만 보므로
// 이 판정을 빼면 잠긴 레벨의 행성이 클릭 가능한 모습으로 그려졌다가 레슨에서 403이 난다.
boolean levelUnlocked = unlockedLevels.contains(selectedLevel);

// 다음 레벨이 이미 열려 있으면 이 레벨은 지나온 레벨이다.
String nextLevel = nextLevel(selectedLevel);
boolean levelPassed = nextLevel != null && unlockedLevels.contains(nextLevel);

// 게이트 상태: 지나온 레벨이면 통과(재응시 가능), 아니면 전 행성 완료 시 응시 가능(ready), 그 외 잠김.
String gateState = !levelUnlocked ? "locked"
        : (levelPassed ? "passed"
                : ((planetCount > 0 && completedPlanets >= planetCount) ? "ready" : "locked"));

// 노드 상태(done/current/open/locked) — 템플릿이 같은 판정을 되풀이하지 않게 여기서 한 번만 계산한다.
Map<Long, String> nodeStates = new LinkedHashMap<>();
for (CurriculumNode node : roadmap) {
    if (!levelUnlocked) {
        nodeStates.put(node.getNodeId(), "locked");
    } else if ("GATE".equals(node.getNodeType())) {
        nodeStates.put(node.getNodeId(), gateState);
    } else if (node.getPlanetNo() == null) {
        nodeStates.put(node.getNodeId(), "locked");
    } else if (node.getPlanetNo() <= completedPlanets) {
        nodeStates.put(node.getNodeId(), "done");
    } else if (levelPassed) {
        nodeStates.put(node.getNodeId(), "open");
    } else if (node.getPlanetNo() == completedPlanets + 1) {
        nodeStates.put(node.getNodeId(), "current");
    } else {
        nodeStates.put(node.getNodeId(), "locked");
    }
}
```

### 발표 포인트

> "우주 지도의 그림 전체가 이 판정 하나에서 나온다. 화면과 서버가 각자 판단하면 반드시 어긋나기 때문에, 상태 결정을 서버 한 곳으로 모으고 템플릿에는 결과 문자열만 넘겼다."

---

## 4. 행성 완료 기준을 노드 플래그가 아니라 레슨 단위 집계로

- **구현 파일**: `ELearning/src/main/java/com/acorn/elearning/learning/service/ProgressService.java`, `ELearning/src/main/resources/mappers/learning/UserLessonProgressMapper.xml`
- **핵심 가치**: 화면의 진행률·완료 행성 수·다음 학습 대상이 모두 같은 집계 하나에서 나오도록 진실의 기준을 한 곳으로 정했다.

### 핵심 설명

- 행성 완료 = 그 행성의 활성·필수 레슨이 1개 이상이고, 전부 이론 학습과 문제풀이를 모두 통과한 상태다.
- 노드 플래그로 판정하던 초기 구현은 레슨 하나만 완료해도 행성이 완료로 떠서, 판정 기준을 레슨 단위 집계로 바꿨다.
- 완료 행성 수는 앞에서부터 연속 완료된 개수만 센다 — 첫 미완료 행성에서 멈추는 순차 학습 규칙이다.
- 개수 세기는 Java 루프 조회가 아니라 SQL `COUNT`로 내려 조회 횟수를 줄였다.

### PPT 코드 발췌

```java
public RoadmapProgress computeRoadmapProgress(Long userId, Long subjectId,
        List<CurriculumNode> roadmap) {
    List<CurriculumNode> planets = roadmap.stream()
            .filter(node -> NODE_TYPE_PLANET.equals(node.getNodeType())
                    && node.getPlanetNo() != null)
            .sorted(Comparator.comparingInt(CurriculumNode::getPlanetNo))
            .toList();

    int planetCount = planets.size();
    int completedPlanets = 0;
    boolean contiguous = true;   // 순차 완료가 끊기면(첫 미완료) 이후로는 completed 카운트 중단
    double rateSum = 0d;

    for (CurriculumNode planet : planets) {
        Long nodeId = planet.getNodeId();
        int required = curriculumService.countRequiredLessons(nodeId);
        int completed = curriculumService.countCompletedRequiredLessons(userId, nodeId);
        boolean planetDone = required > 0 && completed >= required;

        rateSum += (required > 0) ? (completed * 100.0 / required) : 0d;

        if (contiguous) {
            if (planetDone) {
                completedPlanets++;
            } else {
                contiguous = false;
            }
        }
    }

    int progressPercent = (planetCount == 0) ? 0 : (int) Math.round(rateSum / planetCount);
    return new RoadmapProgress(completedPlanets, planetCount, progressPercent);
}
```

```xml
<select id="countCompletedRequiredLessons" resultType="int">
    SELECT COUNT(*)
    FROM lessons l
    JOIN user_lesson_progress ulp
      ON ulp.lesson_id = l.lesson_id AND ulp.user_id = #{userId}
    WHERE l.node_id = #{nodeId}
      AND l.is_active = 1
      AND l.required_for_completion = 1
      AND ulp.theory_completed = 1
      AND ulp.practice_passed = 1
</select>
```

### 발표 포인트

> "'완료'의 기준을 어디에 둘 것인가가 이 파트의 핵심 결정이었다. 노드에 켜 두는 플래그는 조기 완료를 만들어서, 레슨 단위 집계를 유일한 기준으로 삼고 화면 숫자를 전부 여기서만 가져오도록 바꿨다."

---

## 5. 레슨 완료: 진입 권한 확인과 중복 완료를 DB에서 원자적으로 처리

- **구현 파일**: `ELearning/src/main/java/com/acorn/elearning/learning/service/LessonService.java`, `ELearning/src/main/resources/mappers/learning/UserLessonProgressMapper.xml`
- **핵심 가치**: 새로고침·더블 클릭·동시 요청에도 완료 처리가 한 번만 일어나고, 잠긴 레벨의 학습은 화면과 같은 기준으로 차단된다.

### 핵심 설명

- 잠긴 레벨은 로드맵 표시와 같은 `user_level_unlocks` 기준으로 403 처리한다.
- 완료 권한은 조회 후 insert가 아니라 upsert 한 번으로 선점한다 — 이미 완료된 요청은 변경 행 0을 받아 409가 된다.
- 노드 진행 행도 원자 upsert로 갱신해 동시 요청 시 중복 insert가 나지 않게 했다.
- 완료 후에는 문제풀이 잔여 여부로 다음 행동(`START_PRACTICE` / `NEXT_NODE` / 게이트)을 계산해 화면에 넘긴다.

> 이 블록의 원자 선점 방식은 초기 "조회 후 insert" 구현에서 발견된 동시성 문제를 팀에서 함께 정리한 결과다.

### PPT 코드 발췌

```java
// 403: 노드 레벨이 사용자에게 unlock 됐는지 확인(user_level_unlocks). 잠긴 레벨이면 학습 불가.
if (userLevelUnlockMapper.findByUserSubjectLevel(userId, subjectId, node.getLevelCode())
        .isEmpty()) {
    throw new BusinessException(ErrorCode.AUTH_FORBIDDEN, "아직 잠긴 레벨의 학습입니다.");
}

// 레슨 완료 권한을 DB에서 원자적으로 선점한다. 동시에 들어온 두 번째 요청은 0을 받아 409가 된다.
int claimResult = userLessonProgressMapper.claimTheoryCompletion(userId, lessonId);
if (claimResult == 0) {
    throw new BusinessException(ErrorCode.COMMON_IDEMPOTENCY_CONFLICT, "이미 완료한 학습입니다.");
}

// 노드 진행 행도 원자 upsert한다. 기존 "조회 후 insert" 경쟁 조건을 제거한다.
learningProgressMapper.upsertLessonCompleted(userId, subjectId, node.getNodeId());
```

```xml
<!-- 최초 완료 요청만 1 또는 2를 반환하고, 이미 완료된 요청은 0을 반환한다. -->
<insert id="claimTheoryCompletion">
    INSERT INTO user_lesson_progress
        (user_id, lesson_id, theory_completed, practice_passed, progress_rate)
    VALUES (#{userId}, #{lessonId}, 1, 0, 50.00)
    ON DUPLICATE KEY UPDATE
        progress_rate    = IF(theory_completed = 0,
                              IF(practice_passed = 1, 100.00, 50.00), progress_rate),
        completed_at     = IF(theory_completed = 0 AND practice_passed = 1,
                              NOW(), completed_at),
        updated_at       = IF(theory_completed = 0, NOW(), updated_at),
        theory_completed = IF(theory_completed = 0, 1, theory_completed)
</insert>
```

### 발표 포인트

> "'버튼을 두 번 누르면 어떻게 되나'를 애플리케이션 조건문이 아니라 DB 한 문장으로 답했다. 조회와 insert 사이의 빈틈을 없애는 것이 중복 완료를 막는 확실한 방법이었다."

---

## 6. 레벨 해금: 다음 한 단계만, 애플리케이션과 DB 이중 방어

- **구현 파일**: `ELearning/src/main/java/com/acorn/elearning/learning/service/UnlockService.java`, `ELearning/src/main/resources/mappers/learning/UserLevelUnlockMapper.xml`
- **핵심 가치**: 해금은 되돌리기 어려운 이력 데이터이므로, 중복 생성과 단계 건너뛰기를 둘 다 막는다.
- **연동 지점**: AI 코딩테스트가 3문제 중 2문제 이상 통과 시 `unlockNextLevel`을 호출한다.

### 핵심 설명

- AI 코딩테스트 통과 해금은 현재 레벨의 "다음 한 단계"만 연다(BRONZE → SILVER → GOLD, GOLD면 없음).
- 해금 경로는 `unlock_source`로 구분해 기록한다(`ENROLLMENT` / `LEVEL_TEST` / `AI_EXAM_PASS`).
- 애플리케이션에서 기존 해금 여부를 먼저 확인하고, 동시 요청은 DB UNIQUE + `ON DUPLICATE KEY UPDATE`로 흡수한다.
- 충돌 시 실패시키지 않고 `LAST_INSERT_ID`로 기존 행 id를 그대로 돌려주므로 기존 해금 값이 덮어써지지 않는다.

### PPT 코드 발췌

```java
/** (user, subject, level) unlock을 기록한다. 이미 있으면 기존 행을 반환한다(중복 방지). */
@Transactional
public UserLevelUnlock unlock(Long userId, Long subjectId, String levelCode,
                              String unlockSource, Long unlockedByExamId) {
    UserLevelUnlock existing =
            unlockMapper.findByUserSubjectLevel(userId, subjectId, levelCode).orElse(null);
    if (existing != null) {
        return existing;
    }
    ...
}

/** AI 코딩테스트 통과 시: 현재 레벨의 "다음 레벨"을 unlock한다. GOLD면 다음이 없어 null. */
@Transactional
public UserLevelUnlock unlockNextLevel(Long userId, Long subjectId,
        String currentLevelCode, Long examId) {
    String nextLevel = nextLevelOf(currentLevelCode);
    if (nextLevel == null) {
        return null;
    }
    return unlock(userId, subjectId, nextLevel, SOURCE_AI_EXAM_PASS, examId);
}
```

```xml
<!-- (user_id, subject_id, level_code) UNIQUE 기준 멱등: 같은 해금이 동시에 들어와도 실패시키지 않고
     기존 행 id를 그대로 돌려준다(LAST_INSERT_ID). 해금은 한 번 열리면 그만이므로 값을 덮어쓰지 않는다. -->
<insert id="insert" parameterType="com.acorn.elearning.learning.model.UserLevelUnlock"
        useGeneratedKeys="true" keyProperty="unlockId">
    INSERT INTO user_level_unlocks
        (user_id, subject_id, level_code, unlock_source, unlocked_by_exam_id, unlocked_at)
    VALUES
        (#{userId}, #{subjectId}, #{levelCode}, #{unlockSource},
         #{unlockedByExamId}, #{unlockedAt})
    ON DUPLICATE KEY UPDATE
        unlock_id = LAST_INSERT_ID(unlock_id)
</insert>
```

### 발표 포인트

> "해금 로직은 학습 도메인에 있지만 실행 트리거는 코딩테스트 도메인에 있다. 다른 기능의 결과가 도착하는 지점이라 중복 호출을 가정하고 애플리케이션과 DB 양쪽에서 막았다."

---

## 7. 출석과 연속 학습(streak): 트리거는 문제풀이 통과, 하루 1회 멱등

- **구현 파일**: `ELearning/src/main/java/com/acorn/elearning/learning/service/AttendanceService.java`
- **핵심 가치**: 출석의 의미를 "접속"이 아니라 "실제 학습 성과"로 정의하고, 날짜 기준을 고정해 연속 일수가 어긋나지 않게 한다.
- **연동 지점**: 문제풀이 세트 통과 시 해당 도메인이 이 메서드를 호출하고, 기록 자체는 학습 도메인이 저장한다.

### 핵심 설명

- 출석은 이론 레슨 열람이 아니라 문제풀이 세트 통과에만 기록한다 — 팀 논의로 정한 기준이다.
- 하루 1회만 인정하므로 오늘 기록이 있으면 새로 쓰지 않고 기존 기록을 반환한다.
- 직전 출석이 어제면 streak를 +1, 아니면 1로 초기화한다.
- 날짜 계산 기준 시각을 KST로 고정해 서버 타임존에 따라 연속 일수가 달라지지 않게 했다.

### PPT 코드 발췌

```java
@Transactional
public AttendanceRecord recordAttendanceOnPracticePass(Long userId, Long qualifiedSetAttemptId) {
    LocalDate today = LocalDate.now(KST);
    AttendanceRecord latest = attendanceRecordMapper.findLatestByUserId(userId).orElse(null);

    // 오늘 이미 출석 → 그대로 반환 (하루 1회 멱등)
    if (latest != null && today.equals(latest.getAttendanceDate())) {
        return latest;
    }

    // streak: 직전 출석이 어제면 +1, 아니면 1
    int streak = 1;
    if (latest != null && latest.getAttendanceDate() != null
            && latest.getAttendanceDate().equals(today.minusDays(1))) {
        int prev = (latest.getStreakCount() != null) ? latest.getStreakCount() : 0;
        streak = prev + 1;
    }

    AttendanceRecord record = new AttendanceRecord();
    record.setUserId(userId);
    record.setAttendanceDate(today);
    record.setStreakCount(streak);
    record.setQualifiedSetAttemptId(qualifiedSetAttemptId);
    attendanceRecordMapper.insert(record);
    return record;
}
```

### 발표 포인트

> "출석 도장을 접속만으로 찍어 주면 연속 학습 지표가 의미를 잃는다. 문제를 풀어 통과한 날만 기록하고, 시간대는 KST로 고정해 '3일 연속'이 항상 같은 기준으로 계산되게 했다."
