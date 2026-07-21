# Knowva 발표 PPT용 핵심 로직 — 장윤성 (커뮤니티 · 추천 콘텐츠)

> 기준: 각 `##`는 PPT 한 장이다. 화면 정렬이나 단순 CRUD가 아니라, 커뮤니티 데이터가 깨지지 않도록 하는 **상태 전이·정합성·moderation** 중심으로 재선정했다.

## 1. Markdown 임시글을 안전하게 발행하고, 본문에서 지운 이미지는 함께 정리

- **구현 파일**: `ELearning/src/main/java/com/acorn/elearning/community/service/PostService.java`, `AttachmentService.java`, `CommunityPostMapper.xml`
- **핵심 가치**: 사용자가 작성 중인 Markdown과 inline image를 먼저 저장하되, 발행 시점에만 완전한 게시글로 전이한다.

### 핵심 설명

- 에디터 진입 시 제목·본문이 비어 있어도 `DRAFT` 게시글을 먼저 만들어 inline image 업로드의 소유 대상을 확보한다.
- 발행할 때는 작성자 본인의 삭제되지 않은 `DRAFT`인지 확인하고, 제목·본문·과목·게시판 유형을 검증한다.
- SQL update도 `post_id + writer_id + DRAFT + not deleted` 조건을 사용해 다른 사용자의 글이나 이미 발행된 글을 바꾸지 못하게 한다.
- 발행 후 본문 Markdown에서 참조되지 않는 inline image는 metadata와 실제 파일을 함께 제거한다.

### PPT 코드 발췌

```java
public CommunityPost createDraft(SessionUser sessionUser) {
    CommunityPost draft = new CommunityPost();
    draft.setWriterId(requireUserId(sessionUser));
    draft.setSubjectId(1L);
    draft.setBoardType("FREE");
    draft.setTitle("임시 작성 글");
    draft.setContent("");
    draft.setStatus(STATUS_DRAFT);
    communityPostMapper.insert(draft);
    return draft;
}

CommunityPost draft = requireOwnedDraft(form.getDraftPostId(), userId);
draft.setSubjectId(form.getSubjectId());
draft.setBoardType(normalizeBoardType(form.getBoardType()));
draft.setTitle(form.getTitle().trim());
draft.setContent(form.getContent().trim());

if (communityPostMapper.publishDraft(draft) == 0) {
    throw new BusinessException(ErrorCode.COMMON_NOT_FOUND);
}
attachmentService.removeUnreferencedInlineImages(
        draft.getPostId(), draft.getContent());
```

```xml
UPDATE community_posts
SET subject_id = #{subjectId},
    board_type = #{boardType},
    title = #{title}, content = #{content},
    status = 'ACTIVE', updated_at = CURRENT_TIMESTAMP
WHERE post_id = #{postId}
  AND writer_id = #{writerId}
  AND status = 'DRAFT'
  AND deleted_at IS NULL
```

### 발표 포인트

> “본문 이미지가 있는 에디터는 단순 게시글 저장보다 먼저 임시 저장 대상이 필요하다. DRAFT에서 ACTIVE로 바뀌는 조건을 DB까지 제한하고, 본문에서 사라진 이미지도 함께 정리했다.”

---

## 2. 좋아요·스크랩 원본과 게시글 집계 수를 함께 관리

- **구현 파일**: `ELearning/src/main/java/com/acorn/elearning/community/service/ReactionService.java`, `docs/ddl/Knowva_DDL.sql`
- **핵심 가치**: 사용자 반응 원본(`post_likes`, `post_scraps`)과 목록 성능용 집계 수(`like_count`, `scrap_count`)가 어긋나지 않게 한다.

### 핵심 설명

- 좋아요/스크랩은 `(post_id, user_id)` unique key로 한 사용자가 같은 글에 중복 반응을 남길 수 없게 한다.
- 서비스도 이미 반응한 경우 기존 상태를 바로 반환해 반복 클릭을 멱등하게 처리한다.
- 신규 반응이 insert될 때만 게시글 카운터를 증가시키고, 실제 delete가 성공했을 때만 감소시킨다.
- 카운터 감소는 SQL의 `GREATEST(count - 1, 0)`으로 음수를 방지한다.

### PPT 코드 발췌

```java
CommunityPost post = requireActivePost(postId);
if (postLikeMapper.findByPostIdAndUserId(postId, userId).isPresent()) {
    return new ReactionResponse(postId, "LIKE", true,
            safeCount(post.getLikeCount()));
}

PostLike like = new PostLike();
like.setPostId(postId);
like.setUserId(userId);
postLikeMapper.insert(like);
communityPostMapper.incrementLikeCount(postId);
```

```java
int deleted = postLikeMapper.deleteByPostIdAndUserId(postId, userId);
if (deleted > 0) {
    communityPostMapper.decrementLikeCount(postId);
}
```

```sql
CREATE TABLE post_likes (
  like_id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  post_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (like_id),
  UNIQUE KEY uk_post_likes_post_user (post_id, user_id)
);

UPDATE community_posts
SET like_count = GREATEST(like_count - 1, 0)
WHERE post_id = #{postId};
```

### 발표 포인트

> “좋아요 수만 올리는 방식이 아니라, 누가 반응했는지 원본 row를 남긴다. 그 원본과 집계 값을 같은 transaction에서 갱신해 목록 성능과 개인화 상태를 동시에 확보했다.”

---

## 3. 사용자 신고 중복 방지와 관리자 moderation 이력

- **구현 파일**: `ELearning/src/main/java/com/acorn/elearning/community/service/ReportService.java`, `admin/service/AdminCommunityService.java`
- **핵심 가치**: 신고 대상의 존재를 검증하고, 같은 사용자의 같은 대상 신고를 막은 뒤 관리자 상태 변경을 감사 log로 남긴다.

### 핵심 설명

- 신고 대상은 `POST` 또는 `COMMENT`만 허용하며 실제 게시글·댓글 존재 여부를 먼저 확인한다.
- 동일한 사용자·대상 조합의 신고가 있으면 새 row를 만들지 않고 conflict로 종료한다.
- 관리자는 `ACTIVE / HIDDEN / DELETED`만으로 상태를 바꿀 수 있다.
- 상태 변경이 성공하면 관리자 ID, 대상, 변경 상세, 처리 시각을 operation log로 저장한다.

### PPT 코드 발췌

```java
String targetType = form.getTargetType().trim().toUpperCase();
requireTarget(targetType, form.getTargetId());

if (reportMapper.countByTargetAndReporter(
        targetType, form.getTargetId(), reporterId) > 0) {
    throw new BusinessException(ErrorCode.COMMON_IDEMPOTENCY_CONFLICT,
            "이미 신고한 대상입니다.");
}

Report report = new Report();
report.setTargetType(targetType);
report.setTargetId(form.getTargetId());
report.setReporterId(reporterId);
report.setReasonCode(form.getReasonCode().trim().toUpperCase());
report.setStatus(STATUS_RECEIVED);
reportMapper.insert(report);
```

```java
String status = validateStatus(form); // ACTIVE, HIDDEN, DELETED
Long adminId = requireAdminId(sessionUser);
int updated = cm.updateCommentStatus(commentId, status, adminId);
if (updated == 1) {
    adminLogService.insert(operationLog(
            sessionUser,
            "COMMUNITY_COMMENT_STATUS_UPDATE",
            "COMMENT", commentId,
            comment.contentSummary(),
            statusChangeDetail("댓글", status)));
}
```

### 발표 포인트

> “신고는 단순 문의 저장이 아니다. 중복 신고를 막고, 관리자가 콘텐츠를 숨기거나 삭제한 행위는 대상과 관리자 정보를 포함해 별도 감사 log로 남긴다.”

---

## 4. 커뮤니티에서 선택한 과목을 추천 영상·설치 가이드까지 유지

- **구현 파일**: `ELearning/src/main/java/com/acorn/elearning/content/controller/ContentController.java`, `content/service/ContentRecommendationService.java`, `ContentRecommendationMapper.xml`
- **핵심 가치**: 추천 콘텐츠 진입 후에도 현재 과목 컨텍스트를 잃지 않고, 관리자 활성화 상태의 영상과 언어별 설치 가이드를 함께 제공한다.

### 핵심 설명

- `subjectId`가 없으면 Java를 기본값으로 정하고, 있으면 커뮤니티에서 넘겨온 과목 ID를 그대로 사용한다.
- 동일 과목 ID로 VIDEO 추천 목록과 설치 리소스를 동시에 구성한다.
- content type과 slot은 대소문자·공백을 정규화하고, `is_active = 1`인 콘텐츠만 조회한다.
- Python, HTML/CSS/JS, SQL, Java별로 운영체제별 다운로드·설치 절차·버전 확인 명령을 분기한다.

### PPT 코드 발췌

```java
Long activeSubjectId = subjectId == null ? 1L : subjectId;

model.addAttribute("activeSubjectId", activeSubjectId);
model.addAttribute("subjectLabel", subjectLabel(activeSubjectId));
model.addAttribute("videoView",
        contentRecommendationService.recommendations(
                activeSubjectId, "VIDEO", slot));
model.addAttribute("installView", installResource(activeSubjectId));

return "content/recommendations";
```

```java
String normalizedContentType = normalize(contentType);
String normalizedSlot = normalize(slot);

return ContentRecommendationListResponse.of(
        contentRecommendationMapper.findActive(
                subjectId, normalizedContentType, normalizedSlot),
        subjectId, normalizedContentType, normalizedSlot);
```

```xml
SELECT <include refid="columns" />
FROM content_recommendations
WHERE is_active = 1
<if test="subjectId != null">
  AND subject_id = #{subjectId}
</if>
<if test="contentType != null and contentType != ''">
  AND content_type = #{contentType}
</if>
ORDER BY subject_id ASC, recommendation_slot ASC, content_id DESC
```

### 발표 포인트

> “추천 버튼은 단순 영상 재생 버튼이 아니다. 커뮤니티에서 보고 있던 언어를 유지한 채, 추천 영상과 실행 환경 설치·확인 단계까지 연결하는 학습 보조 흐름이다.”
