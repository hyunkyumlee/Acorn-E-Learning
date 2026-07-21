# Knowva 발표 PPT용 핵심 로직 — 송주창 (관리자)


## 1. 관리자 홈 — 운영 지표를 한 화면으로 통합

- **주요 화면**: `/admin` · `templates/admin/dashboard.html`
- **구현 파일**: `admin/controller/AdminController.java`, `admin/service/AdminStatsService.java`
- **핵심 가치**: 회원·학습·제출·신고·공지 데이터를 각각의 메뉴로 들어가지 않고 대시보드에서 즉시 확인한다.

### 핵심 설명

- 대시보드 컨트롤러가 운영에 필요한 요약 수치와 최근 목록을 한 번에 모델에 담는다.
- 카드형 수치뿐 아니라 학습 추이와 과목별 완료율 차트 데이터도 함께 전달한다.
- 화면은 집계 규칙을 알 필요 없이 `AdminStatsService`의 결과만 사용한다.

### PPT 코드 발췌

```java
@GetMapping("/admin")
public String dashboard(Model model) {
    model.addAttribute("totalUserCount", statsService.countTotalUsers());
    model.addAttribute("todayLearningCount", statsService.countTodayLearning());
    model.addAttribute("todaySubmissionCount", statsService.countTodaySubmissions());
    model.addAttribute("pendingReportCount", statsService.countPendingReports());
    model.addAttribute("recentReports", statsService.findRecentReports());
    model.addAttribute("recentNotices", statsService.findRecentNotices());
    model.addAttribute("dailyLearningChart", statsService.dailyLearningChart());
    model.addAttribute("subjectCompleteChart", statsService.subjectCompleteChart());
    return "admin/dashboard";
}
```

### 발표 포인트

> “운영에 필요한 숫자와 최근 이슈를 하나의 진입 화면에 모아, 관리자가 바로 다음 조치를 판단할 수 있게 했다.”

---

## 2. 통계 — 조건에 따라 집계·차트를 다시 생성

- **주요 화면**: `/admin/stats` · `templates/admin/stats.html`
- **구현 파일**: `admin/controller/AdminStatsController.java`, `admin/service/AdminStatsService.java`
- **핵심 가치**: 기간 조건에 따라 학습·제출·시험 데이터를 카드, 표, 차트로 나누어 제공한다.

### 핵심 설명

- `summaryScope`, `periodUnit`, `tableRange`를 요청 파라미터로 받아 조회 범위를 정한다.
- 동일한 기간 조건을 요약 수치·상세 표·차트 서비스 호출에 적용한다.
- 화면은 차트 계산을 직접 하지 않고 서비스가 반환한 데이터만 렌더링한다.

### PPT 코드 발췌

```java
model.addAttribute("totalUsers", service.countUsers(summaryScope));
model.addAttribute("activeUsers", service.countActiveUsers(summaryScope));
model.addAttribute("learningCount", service.countLearning(summaryScope));
model.addAttribute("submissionCount", service.countSubmissions(summaryScope));
model.addAttribute("examAttemptCount", service.countExamAttempts(summaryScope));

model.addAttribute("statsRows", service.findStatsTableRows(null, tableRange));
model.addAttribute("dailyLearningChart", service.dailyLearningChart(periodUnit, null));
model.addAttribute("subjectCompleteChart",
        service.subjectCompleteChart(periodUnit, null, null));
```

### 발표 포인트

> “통계 화면은 단순 카운트가 아니라, 같은 기간 조건으로 표와 차트를 함께 갱신하는 운영 분석 기능이다.”

---

## 3. 과목/커리큘럼 관리 — 학습 구조와 변경 이력 동시 관리

- **주요 화면**: `/admin/courses` · `templates/admin/courses.html`
- **구현 파일**: `admin/service/AdminContentService.java`
- **핵심 가치**: 과목 등록 시 정렬 순서를 자동 부여하고, 관리자 변경 이력을 남긴다.

### 핵심 설명

- 입력 폼을 `Subject` 도메인 객체로 변환한다.
- 기존 과목의 최대 정렬 순서를 찾아 새 과목을 마지막 순서에 배치한다.
- 등록 성공 시 누가 어떤 과목을 등록했는지 운영 로그에 기록한다.

### PPT 코드 발췌

```java
int nextSortOrder = sm.findAll().stream()
        .map(Subject::getSortOrder)
        .filter(Objects::nonNull)
        .mapToInt(Integer::intValue)
        .max()
        .orElse(0) + 1;
s.setSortOrder(nextSortOrder);

int inserted = sm.insert(s);
if (inserted == 1) {
    adminLogService.insert(operationLog(
            adminId, "SUBJECT_CREATE", "SUBJECT",
            s.getSubjectId(), s.getSubjectName(), "과목을 등록"
    ));
}
```

### 발표 포인트

> “과목은 이름만 저장하지 않는다. 학습 노출 순서와 관리자 작업 이력까지 함께 관리한다.”

---

## 4. 이론 자료 관리 — 레슨과 커리큘럼 노드 연결

- **주요 화면**: `/admin/theory` · `templates/admin/theory.html`
- **구현 파일**: `admin/service/AdminContentService.java`, `admin/mapper/AdminLessonMapper.java`
- **핵심 가치**: 이론 레슨을 특정 단원에 연결하고, 필수 이수 여부·순서·활성 상태를 함께 설정한다.

### 핵심 설명

- 레슨은 독립 콘텐츠가 아니라 커리큘럼 노드에 소속된다.
- 필수 이수 여부와 활성 상태의 기본값을 서비스에서 명확히 정한다.
- 레슨 등록 성공 후 운영 로그를 남겨 콘텐츠 변경을 추적한다.

### PPT 코드 발췌

```java
Lesson lesson = new Lesson();
lesson.setNodeId(form.getNodeId());
lesson.setTitle(form.getTitle());
lesson.setContent(form.getContent());
lesson.setRequiredForCompletion(
        form.getRequiredForCompletion() == null ? Boolean.TRUE : form.getRequiredForCompletion()
);
lesson.setSortOrder(form.getSortOrder() == null ? 0 : form.getSortOrder());
lesson.setIsActive(form.getIsActive() == null ? Boolean.TRUE : form.getIsActive());

int inserted = lm.insert(lesson);
```

### 발표 포인트

> “이론 자료는 제목과 본문만 관리하는 것이 아니라, 어느 단원에서 어떤 순서로 이수해야 하는지까지 포함한다.”

---

## 5. 일반 문제 관리 — 레슨 기준으로 과목·단원 관계를 보장

- **주요 화면**: `/admin/problems` · `templates/admin/problems.html`
- **구현 파일**: `admin/service/AdminContentService.java`, `admin/mapper/AdminProblemMapper.java`
- **핵심 가치**: 문제 등록 시 임의의 과목·단원 값을 신뢰하지 않고, 선택한 레슨의 실제 관계로 문제를 저장한다.

### 핵심 설명

- 레슨을 먼저 조회하고 해당 레슨의 단원을 찾는다.
- 단원에서 과목 ID를 구해 문제의 과목·단원·레슨 관계를 일관되게 저장한다.
- 객관식 문제는 본문 저장 뒤 보기 정보를 별도로 저장한다.

### PPT 코드 발췌

```java
Lesson lesson = lm.findById(form.getLessonId())
        .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND));
CurriculumNode node = cm.findById(lesson.getNodeId())
        .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND));

problem.setSubjectId(node.getSubjectId());
problem.setNodeId(lesson.getNodeId());
problem.setLessonId(lesson.getLessonId());
problem.setAnswerText(resolveProblemAnswer(form));

int inserted = ppm.insert(problem);
if (inserted == 1 && "MULTIPLE_CHOICE".equals(problem.getProblemType())) {
    saveProblemChoices(problem.getProblemId(), form);
}
```

### 발표 포인트

> “문제의 과목·단원·레슨을 각각 따로 입력받지 않고, 레슨 기준 관계를 서버에서 확정해 데이터 불일치를 줄였다.”

---

## 6. 추천 콘텐츠 관리 — 등록·상태 변경·삭제를 운영 로그로 추적

- **주요 화면**: `/admin/recommendations` · `templates/admin/recommendations.html`
- **구현 파일**: `admin/service/AdminRecommendationService.java`
- **핵심 가치**: 외부 추천 자료의 변경 내용을 제목·대상·작업 유형과 함께 기록한다.

### 핵심 설명

- 등록·수정 전 공통 `validateForm`으로 URL 등 입력값을 검증한다.
- 수정 시 활성 상태 변화인지 일반 수정인지 구분해 로그 작업 유형을 다르게 남긴다.
- 삭제 전 기존 데이터를 조회해 존재하지 않는 콘텐츠 삭제를 막는다.

### PPT 코드 발췌

```java
URI uri = URI.create(rawUrl.trim());
String scheme = uri.getScheme();

if (!"http".equalsIgnoreCase(scheme)
        && !"https".equalsIgnoreCase(scheme)) {
    throw new BusinessException(
            ErrorCode.COMMON_VALIDATION_FAILED,
            "추천 URL은 http 또는 https 주소만 입력할 수 있습니다."
    );
}

if (uri.getHost() == null || uri.getHost().isBlank()) {
    throw new BusinessException(
            ErrorCode.COMMON_VALIDATION_FAILED,
            "올바른 추천 URL을 입력해 주세요."
    );
}
```

### 발표 포인트

> “추천 콘텐츠는 단순 링크 목록이 아니라, 누가 언제 어떤 자료를 변경했는지 확인 가능한 운영 데이터다.”

---

## 7. 사용자 관리 — 자기 자신과 마지막 관리자를 보호

- **주요 화면**: `/admin/users` · `templates/admin/adminUsers.html`
- **구현 파일**: `admin/service/AdminUserService.java`, `admin/mapper/AdminUserMapper.java`
- **핵심 가치**: 관리자 계정 상태·역할 변경에서 운영자를 모두 잃는 상황을 막는다.

### 핵심 설명

- 관리자는 자기 자신의 상태나 역할을 직접 변경할 수 없다.
- 마지막 활성 관리자를 정지하거나 일반 사용자로 바꾸려 하면 비즈니스 예외를 발생시킨다.
- 실제 변경 성공 후에만 운영 로그를 기록한다.

### PPT 코드 발췌

```java
if (userId.equals(adminId)) {
    throw new BusinessException(ErrorCode.ADMIN_SELF_MODIFY_FORBIDDEN);
}

if ("SUSPENDED".equals(status) && isLastActiveAdmin(user)) {
    throw new BusinessException(ErrorCode.ADMIN_LAST_ADMIN_PROTECTED);
}

int updated = mapper.updateStatus(userId, status);
```

### 발표 포인트

> “관리자 기능에도 안전장치를 두어, 실수로 자기 계정을 막거나 마지막 관리자까지 비활성화하는 일을 방지했다.”

---

## 8. 커뮤니티 관리 — 삭제 대신 상태 전환으로 이력 보존

- **주요 화면**: `/admin/community` · `templates/admin/community.html`
- **구현 파일**: `admin/service/AdminCommunityService.java`, `admin/mapper/AdminCommunityMapper.java`
- **핵심 가치**: 게시글과 댓글을 즉시 삭제하지 않고 상태 전환과 운영 로그로 관리한다.

### 핵심 설명

- 요청된 상태값을 검증한 뒤, 대상 게시글이 실제 존재하는지 확인한다.
- 상태 변경이 성공했을 때만 담당 관리자와 변경 내용을 로그로 남긴다.
- 게시글과 댓글도 같은 패턴으로 관리한다.

### PPT 코드 발췌

```java
String status = validateStatus(form);
AdminCommunityPageResponse.PostItem post = cm.findPostById(postId)
        .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND));

int updated = cm.updatePostStatus(postId, status);
if (updated == 1) {
    adminLogService.insert(operationLog(
            sessionUser, "COMMUNITY_POST_STATUS_UPDATE", "POST",
            postId, post.title(), "게시글 상태를 " + status + "로 변경"
    ));
}
```

### 발표 포인트

> “운영 판단이 필요한 콘텐츠는 삭제보다 상태 전환을 사용해, 사후 확인과 복구 가능성을 남겼다.”

---

## 9. 신고 관리 — 처리 상태와 담당자 이력을 함께 저장

- **주요 화면**: `/admin/reports` · `templates/admin/reports.html`
- **구현 파일**: `admin/service/AdminReportService.java`, `admin/mapper/AdminReportMapper.java`
- **핵심 가치**: 신고 처리 전 대상을 검증하고, 상태 변경과 처리 이력을 한 트랜잭션에서 관리한다.

### 핵심 설명

- 존재하지 않는 신고를 처리하려는 요청은 즉시 404 성격의 비즈니스 오류로 막는다.
- 상태 변경 성공 여부를 확인한 뒤에만 운영 로그를 추가한다.
- 로그에는 관리자, 신고 대상 요약, 처리 결과가 포함된다.

### PPT 코드 발췌

```java
ReportPageResponse.ReportItem report = rm.findById(reportId)
        .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND));

int updated = rm.updateStatus(reportId, form.getStatus());
if (updated == 1) {
    adminLogService.insert(operationLog(
            sessionUser, reportId, report.targetSummary(),
            reportChangeDetail(form.getStatus())
    ));
}
```

### 발표 포인트

> “신고 처리는 상태만 바꾸는 기능이 아니라, 누가 어떤 판단을 했는지 남기는 운영 프로세스다.”

---

## 10. 공지사항 관리 — 공지 변경도 감사 로그로 관리

- **주요 화면**: `/admin/notices` · `templates/admin/notices.html`
- **구현 파일**: `admin/service/AdminNoticeService.java`, `admin/mapper/NoticeMapper.java`
- **핵심 가치**: 등록·수정·삭제된 공지의 대상과 작업 내용을 운영 로그로 남긴다.

### 핵심 설명

- 수정 전 공지를 조회해 존재 여부를 확인한다.
- DB 수정 성공 시에만 로그를 남겨 실패한 작업이 이력에 섞이지 않게 한다.
- 공지 작성자와 변경 관리자를 분리해 운영 책임을 추적할 수 있다.

### PPT 코드 발췌

```java
@Transactional
public int update(Notice model, SessionUser sessionUser) {
    int updated = mapper.update(model);
    if (updated == 1) {
        adminLogService.insert(operationLog(
                sessionUser, "NOTICE_UPDATE", model.getNoticeId(),
                model.getTitle(), "공지사항을 수정"
        ));
    }
    return updated;
}
```

### 발표 포인트

> “공지사항도 운영자가 바꾼 중요한 서비스 정보이므로, CRUD 처리와 변경 이력을 함께 관리한다.”

---

## 11. 관리자 운영 로그 — 모든 운영 변경의 공통 감사 지점

- **주요 화면**: `/admin/operation-logs` · `templates/admin/adminLog.html`
- **구현 파일**: `admin/service/AdminLogService.java`, `admin/service/AdminOpsService.java`, `admin/mapper/AdminOperationLogMapper.java`
- **핵심 가치**: 사용자 상태, 콘텐츠, 신고, 공지의 변경 기록을 한 화면에서 검색·추적한다.

### 핵심 설명

- 각 도메인 서비스는 변경 성공 후 `AdminLogService`에 공통 로그 객체를 전달한다.
- 운영 로그 화면은 페이지·검색 조건을 받아 이력을 조회한다.
- 기능별 로그 테이블을 따로 만들지 않아, 감사 기준이 일관된다.

### PPT 코드 발췌

```java
public int insert(AdminOperationLog model) {
    if (model == null) {
        return 0;
    }
    return mapper.insert(model);
}

// 예: 사용자 역할 변경 성공 후 호출
service.insert(operationLog(
        adminId, "USER_ROLE_UPDATE", userId,
        user.getEmail(), changeDetail
));
```

### 발표 포인트

> “관리자 기능의 공통 핵심은 변경 자체보다 변경을 추적하는 것이다. 운영 로그가 모든 관리 기능을 연결하는 감사 기반이다.”


