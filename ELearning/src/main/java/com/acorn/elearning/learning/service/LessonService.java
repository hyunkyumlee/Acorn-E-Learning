package com.acorn.elearning.learning.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.learning.mapper.CurriculumNodeMapper;
import com.acorn.elearning.learning.mapper.LearningProgressMapper;
import com.acorn.elearning.learning.mapper.LessonMapper;
import com.acorn.elearning.learning.mapper.UserLevelUnlockMapper;
import com.acorn.elearning.learning.model.CurriculumNode;
import com.acorn.elearning.learning.model.LearningProgress;
import com.acorn.elearning.learning.model.Lesson;
import com.acorn.elearning.learning.view.LessonProgressView;
import com.acorn.elearning.security.SessionUser;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 이론 lesson(SR-005) 서비스. 현재 브랜치 범위 = completeLesson(LEARN-005) 완료 처리.
 *
 * completeLesson은 <b>learning_progress만 write</b>한다. (출석 X)
 *   ⚠️ 출석(attendance_records) 기록은 이 흐름이 아니라 "문제풀이 세트 7/10 통과"(SR-006, 3번 흐름)에서
 *      일어난다. DB 명세 v1.4 attendance_records 비고: "KST 기준 10문제 세트 7/10 이상 1일 1회 인정."
 *      + qualified_set_attempt_id → practice_set_attempts FK. (분담범위 §6의 옛 예시코드는 명세로 정정됨)
 *      출석 write owner는 여전히 2번이지만 트리거 위치는 practice → 3번이 AttendanceService를 호출하는 교차 계약.
 *
 * 채점/점수: 이론 완료는 score_events를 지급하지 않는다(점수는 3번 ScoreService 소관).
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

    public LessonService(LessonMapper lessonMapper,
                         CurriculumNodeMapper curriculumNodeMapper,
                         LearningProgressMapper learningProgressMapper,
                         UserLevelUnlockMapper userLevelUnlockMapper) {
        this.lessonMapper = lessonMapper;
        this.curriculumNodeMapper = curriculumNodeMapper;
        this.learningProgressMapper = learningProgressMapper;
        this.userLevelUnlockMapper = userLevelUnlockMapper;
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

        // 409: 이미 이론을 완료한 단원(재완료 방지).
        // TODO(공통 owner 이현겸): 전용 코드 LEARNING-ALREADY-COMPLETED 신설 시 교체. 지금은 409 상태만 맞춰 재사용.
        if (progress != null && Boolean.TRUE.equals(progress.getLessonCompleted())) {
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
}
