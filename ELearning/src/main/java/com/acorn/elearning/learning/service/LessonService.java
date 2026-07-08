package com.acorn.elearning.learning.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.learning.dto.response.LessonBookmarkPageResponse;
import com.acorn.elearning.learning.dto.response.LessonBookmarkResponse;
import com.acorn.elearning.learning.dto.response.LessonDetailResponse;
import com.acorn.elearning.learning.mapper.CurriculumNodeMapper;
import com.acorn.elearning.learning.mapper.LearningProgressMapper;
import com.acorn.elearning.learning.mapper.LessonBookmarkMapper;
import com.acorn.elearning.learning.mapper.LessonMapper;
import com.acorn.elearning.learning.mapper.UserLessonProgressMapper;
import com.acorn.elearning.learning.mapper.UserLevelUnlockMapper;
import com.acorn.elearning.learning.model.CurriculumNode;
import com.acorn.elearning.learning.model.LearningProgress;
import com.acorn.elearning.learning.model.Lesson;
import com.acorn.elearning.learning.model.LessonBookmark;
import com.acorn.elearning.learning.view.LessonProgressView;
import com.acorn.elearning.security.SessionUser;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 이론 lesson 완료 처리 + 레슨 북마크 서비스.
 * completeLesson은 learning_progress만 write한다(출석/점수는 이 흐름에서 다루지 않음).
 */
@Service
public class LessonService {

    /** 단원 진행률 규약(임시): 이론 완료=50, 문제풀이까지 통과=100. (명세에 공식 없음 → 팀장 확인 대상) */
    private static final BigDecimal RATE_LESSON_ONLY = new BigDecimal("50.00");
    private static final BigDecimal RATE_FULL = new BigDecimal("100.00");

    private final LessonMapper lessonMapper;
    private final CurriculumNodeMapper curriculumNodeMapper;
    private final LearningProgressMapper learningProgressMapper;
    private final UserLevelUnlockMapper userLevelUnlockMapper;
    private final LessonBookmarkMapper lessonBookmarkMapper;
    private final UserLessonProgressMapper userLessonProgressMapper;

    public LessonService(LessonMapper lessonMapper,
                         CurriculumNodeMapper curriculumNodeMapper,
                         LearningProgressMapper learningProgressMapper,
                         UserLevelUnlockMapper userLevelUnlockMapper,
                         LessonBookmarkMapper lessonBookmarkMapper,
                         UserLessonProgressMapper userLessonProgressMapper) {
        this.lessonMapper = lessonMapper;
        this.curriculumNodeMapper = curriculumNodeMapper;
        this.learningProgressMapper = learningProgressMapper;
        this.userLevelUnlockMapper = userLevelUnlockMapper;
        this.lessonBookmarkMapper = lessonBookmarkMapper;
        this.userLessonProgressMapper = userLessonProgressMapper;
    }

    /**
     * LEARN-005: 이론 lesson 완료 처리. learning_progress를 (user, subject, node) UNIQUE 기준으로 upsert한다.
     * 한 트랜잭션으로 묶는다.
     *
     * 에러(공통 ErrorCode 재사용 — learning 전용 코드 신설은 공통 owner 몫이라 여기서 안 만든다):
     *   - 레슨/단원 없음 → COMMON_NOT_FOUND (REST 404 LEARNING-LESSON-404 대응)
     *   - 미해금 레벨 → AUTH_FORBIDDEN   (REST 403 LEARNING-LEVEL-LOCKED 대응)
     *   - 이미 완료   → COMMON_IDEMPOTENCY_CONFLICT(409) + 커스텀 메시지 (REST 409 LEARNING-ALREADY-COMPLETED 대응)
     */
    @Transactional
    public LessonProgressView completeLesson(SessionUser user, Long lessonId) {
        Long userId = user.userId();

        Lesson lesson = lessonMapper.findById(lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "레슨을 찾을 수 없습니다."));
        CurriculumNode node = curriculumNodeMapper.findById(lesson.getNodeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "단원 정보를 찾을 수 없습니다."));
        Long subjectId = node.getSubjectId();

        // 403: 노드 레벨이 사용자에게 unlock 됐는지 확인(user_level_unlocks). 잠긴 레벨이면 학습 불가.
        if (userLevelUnlockMapper.findByUserSubjectLevel(userId, subjectId, node.getLevelCode()).isEmpty()) {
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN, "아직 잠긴 레벨의 학습입니다.");
        }

        LearningProgress progress = learningProgressMapper
                .findByUserSubjectNode(userId, subjectId, node.getNodeId())
                .orElse(null);

        // 409: 이미 이론을 완료한 '레슨'(레슨 단위 재완료 방지). user_lesson_progress 기준.
        boolean alreadyTheoryDone = userLessonProgressMapper.findByUserAndLesson(userId, lessonId)
                .map(p -> Boolean.TRUE.equals(p.getTheoryCompleted()))
                .orElse(false);
        if (alreadyTheoryDone) {
            throw new BusinessException(ErrorCode.COMMON_IDEMPOTENCY_CONFLICT, "이미 완료한 학습입니다.");
        }

        boolean practicePassed = progress != null && Boolean.TRUE.equals(progress.getPracticePassed());
        BigDecimal rate = practicePassed ? RATE_FULL : RATE_LESSON_ONLY;
        LocalDateTime now = LocalDateTime.now();

        if (progress == null) {
            // 진행 행이 없던 단원 → 새 행 insert. 이론만 완료라 단원 완전완료(completed_at)는 아직 아님.
            LearningProgress row = new LearningProgress();
            row.setUserId(userId);
            row.setSubjectId(subjectId);
            row.setNodeId(node.getNodeId());
            row.setLessonCompleted(true);
            row.setPracticePassed(false);
            row.setProgressRate(rate);
            row.setCompletedAt(null);
            learningProgressMapper.insert(row);
        } else {
            // 문제풀이만 되어 있던 단원 → 이론 완료로 갱신. 둘 다 되면 단원 완전완료 시점 기록.
            progress.setLessonCompleted(true);
            progress.setProgressRate(rate);
            if (practicePassed) {
                progress.setCompletedAt(now);
            }
            learningProgressMapper.update(progress);
        }

        // 레슨 단위 진행 기록(source of truth). 레슨 선택 화면·로드맵 집계가 이 값을 읽는다.
        userLessonProgressMapper.upsertTheoryCompleted(userId, lessonId);

        // nextAction 힌트: 문제풀이가 남았으면 START_PRACTICE, 아니면 다음 단원/게이트.
        String nextAction;
        Long nextNodeId = null;
        if (!practicePassed) {
            nextAction = "START_PRACTICE";
        } else {
            CurriculumNode next = curriculumNodeMapper
                    .findNextNode(subjectId, node.getLevelCode(), node.getSortOrder(), node.getNodeId())
                    .orElse(null);
            if (next != null) {
                nextAction = "NEXT_NODE";
                nextNodeId = next.getNodeId();
            } else {
                nextAction = "GATE";
            }
        }

        return new LessonProgressView(node.getNodeId(), true, rate.intValue(), nextAction, nextNodeId);
    }

    /**
     * 레슨 상세 조회(REST): 이론 본문 + 북마크 여부 + 단원 진행.
     * 레슨/단원 없으면 404, 잠긴 레벨이면 403(완료 처리와 동일 기준).
     */
    @Transactional(readOnly = true)
    public LessonDetailResponse getLessonDetailResponse(SessionUser user, Long lessonId) {
        Long userId = user.userId();

        Lesson lesson = lessonMapper.findById(lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "레슨을 찾을 수 없습니다."));
        CurriculumNode node = curriculumNodeMapper.findById(lesson.getNodeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "단원 정보를 찾을 수 없습니다."));
        Long subjectId = node.getSubjectId();

        if (userLevelUnlockMapper.findByUserSubjectLevel(userId, subjectId, node.getLevelCode()).isEmpty()) {
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN, "아직 잠긴 레벨의 학습입니다.");
        }

        boolean bookmarked = lessonBookmarkMapper.findByUserAndLesson(userId, lessonId).isPresent();
        LearningProgress progress = learningProgressMapper
                .findByUserSubjectNode(userId, subjectId, node.getNodeId())
                .orElse(null);

        LessonDetailResponse.Progress progressView = new LessonDetailResponse.Progress(
                progress != null && Boolean.TRUE.equals(progress.getLessonCompleted()),
                progress != null && Boolean.TRUE.equals(progress.getPracticePassed()),
                (progress != null && progress.getProgressRate() != null)
                        ? progress.getProgressRate().intValue() : 0);

        return new LessonDetailResponse(
                lesson.getLessonId(), lesson.getNodeId(), lesson.getTitle(),
                lesson.getContent(), lesson.getExampleCode(), bookmarked, progressView);
    }

    /** LEARN-006: 레슨 북마크 추가. 레슨 없으면 404, 이미 북마크면 409. */
    @Transactional
    public LessonBookmarkResponse addBookmark(SessionUser user, Long lessonId) {
        Long userId = user.userId();
        lessonMapper.findById(lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "레슨을 찾을 수 없습니다."));
        if (lessonBookmarkMapper.findByUserAndLesson(userId, lessonId).isPresent()) {
            throw new BusinessException(ErrorCode.COMMON_IDEMPOTENCY_CONFLICT, "이미 북마크한 레슨입니다.");
        }
        LessonBookmark bookmark = new LessonBookmark();
        bookmark.setUserId(userId);
        bookmark.setLessonId(lessonId);
        lessonBookmarkMapper.insert(bookmark);
        return new LessonBookmarkResponse(lessonId, true);
    }

    /** LEARN-006: 레슨 북마크 해제. 레슨 없으면 404. 없는 북마크 삭제는 그대로 성공 처리. */
    @Transactional
    public LessonBookmarkResponse removeBookmark(SessionUser user, Long lessonId) {
        Long userId = user.userId();
        lessonMapper.findById(lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "레슨을 찾을 수 없습니다."));
        lessonBookmarkMapper.deleteByUserAndLesson(userId, lessonId);
        return new LessonBookmarkResponse(lessonId, false);
    }

    /** LEARN-007: 내 북마크 목록(과목 필터 optional, page/size 페이지네이션). */
    @Transactional(readOnly = true)
    public LessonBookmarkPageResponse getBookmarks(SessionUser user, Long subjectId, int page, int size) {
        Long userId = user.userId();
        int safePage = Math.max(page, 1);
        int safeSize = (size < 1) ? 20 : Math.min(size, 100);
        int offset = (safePage - 1) * safeSize;

        long total = lessonBookmarkMapper.countByUser(userId, subjectId);
        List<LessonBookmarkPageResponse.Item> items = lessonBookmarkMapper
                .findPageByUser(userId, subjectId, safeSize, offset).stream()
                .map(v -> new LessonBookmarkPageResponse.Item(
                        v.getLessonId(), v.getLessonTitle(), v.getNodeId(), v.getNodeTitle(),
                        v.getSubjectId(), v.getLevelCode(), v.getBookmarkedAt()))
                .toList();
        return new LessonBookmarkPageResponse(total, safePage, safeSize, items);
    }

    /** 레슨 북마크 여부(화면 버튼 상태 표시용). */
    @Transactional(readOnly = true)
    public boolean isBookmarked(SessionUser user, Long lessonId) {
        return lessonBookmarkMapper.findByUserAndLesson(user.userId(), lessonId).isPresent();
    }

    /** 화면 버튼용 북마크 토글: 있으면 해제, 없으면 추가. 레슨 없으면 404. */
    @Transactional
    public LessonBookmarkResponse toggleBookmark(SessionUser user, Long lessonId) {
        lessonMapper.findById(lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "레슨을 찾을 수 없습니다."));
        Long userId = user.userId();
        if (lessonBookmarkMapper.findByUserAndLesson(userId, lessonId).isPresent()) {
            lessonBookmarkMapper.deleteByUserAndLesson(userId, lessonId);
            return new LessonBookmarkResponse(lessonId, false);
        }
        LessonBookmark bookmark = new LessonBookmark();
        bookmark.setUserId(userId);
        bookmark.setLessonId(lessonId);
        lessonBookmarkMapper.insert(bookmark);
        return new LessonBookmarkResponse(lessonId, true);
    }
}
