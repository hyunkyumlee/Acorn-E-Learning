package com.acorn.elearning.learning.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.learning.form.LevelTestForm;
import com.acorn.elearning.learning.mapper.LearningProfileWriteMapper;
import com.acorn.elearning.learning.mapper.LevelTestAnswerMapper;
import com.acorn.elearning.learning.mapper.LevelTestAttemptMapper;
import com.acorn.elearning.learning.mapper.LevelTestChoiceMapper;
import com.acorn.elearning.learning.mapper.LevelTestQuestionMapper;
import com.acorn.elearning.learning.mapper.UserLevelUnlockMapper;
import com.acorn.elearning.learning.model.LevelTestAnswer;
import com.acorn.elearning.learning.model.LevelTestAttempt;
import com.acorn.elearning.learning.model.LevelTestChoice;
import com.acorn.elearning.learning.model.LevelTestQuestion;
import com.acorn.elearning.learning.model.UserLevelUnlock;
import com.acorn.elearning.learning.view.LevelTestQuestionView;
import com.acorn.elearning.learning.view.LevelTestResultView;
import com.acorn.elearning.security.SessionUser;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 레벨 테스트(SR-004, LEVEL-001/002) 서비스.
 * 흐름: 문항 조회(getQuestions) → 제출/채점/반영(submitAndApply) → 결과 조회(getResult).
 *
 * 채점 규칙(분담범위 v2.4 / 매핑표 v0.7): 8문항, 정답 개수 기준
 *   0-2 Bronze / 3-5 Silver / 6-8 Gold.
 * 반영 대상: user_learning_profiles.current_level_code(레벨) + user_level_unlocks(언락 이력).
 * ⚠️ 레벨 테스트는 랭킹 점수(score_events)를 지급하지 않는다(매핑표: SR-004는 score_events 대상 아님).
 *    점수 지급은 3번(고지연) ScoreService 소관이므로 여기서 건드리지 않는다.
 * ⚠️ AI 미사용.
 */
@Service
public class LevelTestService {

    // 레벨 코드(user_learning_profiles.current_level_code / user_level_unlocks.level_code 공통 문자열 규약)
    private static final String LEVEL_BRONZE = "BRONZE";
    private static final String LEVEL_SILVER = "SILVER";
    private static final String LEVEL_GOLD = "GOLD";

    /**
     * user_level_unlocks.unlock_source 값. DB 명세 enum: LEVEL_TEST / AI_EXAM_PASS / ADMIN_ADJUST.
     * 레벨 테스트로 인한 unlock = 'LEVEL_TEST'. (샘플데이터의 'ONBOARDING'/'AI_EXAM'은 명세 이전 값)
     */
    private static final String UNLOCK_SOURCE_LEVEL_TEST = "LEVEL_TEST";

    private static final String ATTEMPT_STATUS_SUBMITTED = "SUBMITTED";

    private final LevelTestQuestionMapper questionMapper;
    private final LevelTestChoiceMapper choiceMapper;
    private final LevelTestAttemptMapper attemptMapper;
    private final LevelTestAnswerMapper answerMapper;
    private final UserLevelUnlockMapper unlockMapper;
    private final LearningProfileWriteMapper profileWriteMapper;

    public LevelTestService(LevelTestQuestionMapper questionMapper,
                            LevelTestChoiceMapper choiceMapper,
                            LevelTestAttemptMapper attemptMapper,
                            LevelTestAnswerMapper answerMapper,
                            UserLevelUnlockMapper unlockMapper,
                            LearningProfileWriteMapper profileWriteMapper) {
        this.questionMapper = questionMapper;
        this.choiceMapper = choiceMapper;
        this.attemptMapper = attemptMapper;
        this.answerMapper = answerMapper;
        this.unlockMapper = unlockMapper;
        this.profileWriteMapper = profileWriteMapper;
    }

    /**
     * LEVEL-001: 특정 과목의 활성 문항 + 선택지를 화면 표시용 View로 조립한다.
     * 정답 여부(is_correct)는 View에 담지 않는다(화면 소스 노출 방지).
     */
    @Transactional(readOnly = true)
    public List<LevelTestQuestionView> getQuestions(Long subjectId) {
        List<LevelTestQuestion> questions = questionMapper.findActiveBySubjectId(subjectId);
        if (questions.isEmpty()) {
            // 해당 과목에 활성 레벨 테스트 문항이 없음(현재 샘플은 JAVA 과목만 출제됨).
            throw new BusinessException(ErrorCode.COMMON_NOT_FOUND, "해당 과목의 레벨 테스트 문항이 없습니다.");
        }

        List<LevelTestQuestionView> views = new ArrayList<>(questions.size());
        for (LevelTestQuestion q : questions) {
            List<LevelTestQuestionView.ChoiceView> choiceViews = choiceMapper.findByQuestionId(q.getQuestionId())
                    .stream()
                    .map(c -> new LevelTestQuestionView.ChoiceView(
                            c.getChoiceId(), c.getChoiceLabel(), c.getChoiceText()))
                    .toList();
            views.add(new LevelTestQuestionView(
                    q.getQuestionId(), q.getQuestionNo(), q.getQuestionText(), choiceViews));
        }
        return views;
    }

    /**
     * LEVEL-002: 답안을 채점해 attempt/answers 저장 + 프로필 레벨 + unlock 이력을 반영한다.
     * 한 트랜잭션으로 묶는다(중간 실패 시 전체 롤백).
     *
     * @return 결과 View(등급/정답수). Controller는 attemptId로 결과 화면 redirect에 사용한다.
     */
    @Transactional
    public LevelTestResultView submitAndApply(SessionUser user, LevelTestForm form) {
        Long userId = user.userId();
        Long subjectId = form.getSubjectId();

        // 채점 기준(정답)은 폼이 아니라 DB의 활성 문항 집합을 신뢰한다.
        List<LevelTestQuestion> questions = questionMapper.findActiveBySubjectId(subjectId);
        if (questions.isEmpty()) {
            throw new BusinessException(ErrorCode.COMMON_NOT_FOUND, "해당 과목의 레벨 테스트 문항이 없습니다.");
        }

        Map<Long, Long> submitted = (form.getAnswers() != null) ? form.getAnswers() : Map.of();

        // 제출 검증(REST LEVEL-002 규칙 LEVEL-ANSWER-COUNT-INVALID): 모든 문항에 응답해야 한다.
        for (LevelTestQuestion q : questions) {
            if (submitted.get(q.getQuestionId()) == null) {
                throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "모든 문항에 답해야 합니다.");
            }
        }

        // 1) attempt 먼저 생성해 attempt_id 확보(answers FK).
        int totalCount = questions.size();
        LevelTestAttempt attempt = new LevelTestAttempt();
        attempt.setUserId(userId);
        attempt.setSubjectId(subjectId);
        attempt.setTotalCount(totalCount);
        attempt.setCorrectCount(0);              // 채점 후 update
        attempt.setResultLevelCode(LEVEL_BRONZE); // 채점 후 update
        attempt.setStatus(ATTEMPT_STATUS_SUBMITTED);
        attempt.setSubmittedAt(LocalDateTime.now());
        attemptMapper.insert(attempt); // useGeneratedKeys → attempt.attemptId 채워짐
        Long attemptId = attempt.getAttemptId();

        // 2) 문항별 채점 + answer 저장.
        int correctCount = 0;
        for (LevelTestQuestion q : questions) {
            Long correctChoiceId = correctChoiceId(q.getQuestionId());
            Long submittedChoiceId = submitted.get(q.getQuestionId());
            boolean isCorrect = submittedChoiceId != null && submittedChoiceId.equals(correctChoiceId);
            if (isCorrect) {
                correctCount++;
            }

            LevelTestAnswer answer = new LevelTestAnswer();
            answer.setAttemptId(attemptId);
            answer.setQuestionId(q.getQuestionId());
            answer.setChoiceId(submittedChoiceId); // 미응답이면 null
            answer.setSubmittedAnswer(null);        // 객관식이라 텍스트 답안 미사용
            answer.setIsCorrect(isCorrect);
            answerMapper.insert(answer);
        }

        // 3) 등급 산정 후 attempt 갱신.
        String resultLevel = grade(correctCount);
        attempt.setCorrectCount(correctCount);
        attempt.setResultLevelCode(resultLevel);
        attemptMapper.update(attempt);

        // 4) 프로필 레벨 반영(learning 전용 write 매퍼 — 6번 공유 매퍼 미사용).
        profileWriteMapper.updateLevel(userId, resultLevel);

        // 5) unlock 이력 반영(멱등). DB 명세: "Bronze는 레벨 테스트 이후 활성화, Silver/Gold unlock은 AI 시험 2/3 통과 시 생성".
        //    → 레벨 테스트 결과가 Silver/Gold여도 여기서 여는 unlock 행은 BRONZE 1건. (상위 레벨 언락은 AI 시험 소관)
        //    ⚠️ REST 응답은 unlockedLevels[](복수)이나 행 개수 규칙이 명세에 확정 안 됨 → 팀장 확인 대상(현재는 DB 비고 기준 BRONZE).
        upsertUnlock(userId, subjectId, LEVEL_BRONZE);

        return new LevelTestResultView(attemptId, subjectId, resultLevel, correctCount, totalCount);
    }

    /**
     * LEVEL-002 결과 화면: attempt 저장 결과를 그대로 View로 반환한다.
     * 본인 attempt만 조회 가능하다.
     */
    @Transactional(readOnly = true)
    public LevelTestResultView getResult(SessionUser user, Long attemptId) {
        LevelTestAttempt attempt = attemptMapper.findById(attemptId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "레벨 테스트 결과를 찾을 수 없습니다."));
        if (!attempt.getUserId().equals(user.userId())) {
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN, "본인의 레벨 테스트 결과만 볼 수 있습니다.");
        }
        return new LevelTestResultView(
                attempt.getAttemptId(),
                attempt.getSubjectId(),
                attempt.getResultLevelCode(),
                attempt.getCorrectCount(),
                attempt.getTotalCount());
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

    /** 문항의 정답 choice_id(is_correct=1). 없으면 null(데이터 이상). */
    private Long correctChoiceId(Long questionId) {
        return choiceMapper.findByQuestionId(questionId).stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsCorrect()))
                .map(LevelTestChoice::getChoiceId)
                .findFirst()
                .orElse(null);
    }

    /** (user, subject, level) UNIQUE 기준 멱등 insert. 이미 있으면 아무 것도 하지 않는다. */
    private void upsertUnlock(Long userId, Long subjectId, String levelCode) {
        if (unlockMapper.findByUserSubjectLevel(userId, subjectId, levelCode).isPresent()) {
            return;
        }
        UserLevelUnlock unlock = new UserLevelUnlock();
        unlock.setUserId(userId);
        unlock.setSubjectId(subjectId);
        unlock.setLevelCode(levelCode);
        unlock.setUnlockSource(UNLOCK_SOURCE_LEVEL_TEST);
        unlock.setUnlockedByExamId(null); // 레벨 테스트는 exam_sessions와 무관(AI 시험만 참조)
        unlock.setUnlockedAt(LocalDateTime.now());
        unlockMapper.insert(unlock);
    }
}
