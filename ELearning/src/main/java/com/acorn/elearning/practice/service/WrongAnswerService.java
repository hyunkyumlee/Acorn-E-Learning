package com.acorn.elearning.practice.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.acorn.elearning.practice.form.WrongAnswerRetryForm;
import com.acorn.elearning.practice.mapper.PracticeProblemMapper;
import com.acorn.elearning.practice.mapper.PracticeSubmissionMapper;
import com.acorn.elearning.practice.mapper.WrongAnswerMapper;
import com.acorn.elearning.practice.model.PracticeProblem;
import com.acorn.elearning.practice.model.PracticeSubmission;
import com.acorn.elearning.practice.model.WrongAnswer;
import com.acorn.elearning.practice.view.WrongAnswerDetailView;
import com.acorn.elearning.practice.view.WrongAnswerPageView;
import com.acorn.elearning.practice.view.WrongAnswerSummaryView;
import com.acorn.elearning.security.SessionUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WrongAnswerService {
    /*
    public Map<String, Object> stub(String action) {
        // TODO 구현 예시입니다. 실제 parameter와 return DTO로 method signature를 교체하세요.
        // SessionUser sessionUser = currentSessionUser();
        // Object entity = domainMapper.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND));
        // domainMapper.update(applyForm(entity, form));
        // return Map.of("result", entity);
        return Map.of("action", action, "status", "SKELETON");
    }
    */

    private final WrongAnswerMapper wrongAnswerMapper;
    private final PracticeProblemMapper practiceProblemMapper;
    private final PracticeSubmissionMapper practiceSubmissionMapper;

    public WrongAnswerService(WrongAnswerMapper wrongAnswerMapper, PracticeProblemMapper practiceProblemMapper, PracticeSubmissionMapper practiceSubmissionMapper) {

        this.wrongAnswerMapper = wrongAnswerMapper;
        this.practiceProblemMapper = practiceProblemMapper;
        this.practiceSubmissionMapper = practiceSubmissionMapper;
    }

    // 오답을 데이터베이스에 기록합니다.
    @Transactional
    public void recordWrongAnswer(Long setAttemptId, Long userId, Long problemId, Long submissionId) {
        WrongAnswer wrongAnswer = new WrongAnswer();
        wrongAnswer.setSetAttemptId(setAttemptId);
        wrongAnswer.setUserId(userId);
        wrongAnswer.setProblemId(problemId);

        // 1. 오답 횟수 초기화 (처음 틀렸을 때는 1)
        wrongAnswer.setWrongCount(1);

        // 2. 제출 ID 기록 (나중에 상세 오답 노트 볼 때 참조) --// 모델에 있는 필드 활용 [cite: 649]
        wrongAnswer.setLastSubmissionId(submissionId);

        // 3. 상태 초기화
        wrongAnswer.setReviewStatus("PENDING");

        // 4. 재정답 보너스 여부 초기화
        wrongAnswer.setRetryBonusAwarded(false);

        wrongAnswerMapper.insertWrongAnswer(wrongAnswer);
    }


    // 사용자 ID로 오답 목록 가져오기
    public List<WrongAnswer> getWrongAnswersByUserId(Long userId) {

        return wrongAnswerMapper.findAllWrongAnswersByUserId(userId);
    }


/*
    // 2. 오답 해결 --상태변경
    @Transactional
    public int solveWrongAnswer(SessionUser user, Long wrongAnswerId) {
        return 0;
    }
*/
    //오답목록조회
    public WrongAnswerSummaryView summary(SessionUser sessionUser) {
        List<WrongAnswer> wrongAnswers =
                wrongAnswerMapper.findAllWrongAnswersByUserId(sessionUser.userId());

        int total = wrongAnswers.size();

        int solved = (int) wrongAnswers.stream()
                .filter(w -> "SOLVED".equals(w.getReviewStatus()))
                .count();

        int pending = total - solved;

        return WrongAnswerSummaryView.from(total, pending, solved);
    }

    public WrongAnswerPageView list(SessionUser sessionUser) {
        List<WrongAnswer> wrongAnswers =
                wrongAnswerMapper.findAllWrongAnswersByUserId(sessionUser.userId());

        List<Map<String, Object>> items = wrongAnswers.stream()
                .map(w -> {
                    PracticeProblem problem = practiceProblemMapper.findById(w.getProblemId())
                            .orElseThrow(() -> new RuntimeException("문제를 찾을 수 없습니다."));

                    Map<String, Object> item = new HashMap<>();
                    item.put("wrongAnswerId", w.getWrongAnswerId());
                    item.put("problemId", w.getProblemId());
                    item.put("question", problem.getQuestion());
                    item.put("wrongCount", w.getWrongCount());
                    item.put("reviewStatus", w.getReviewStatus());
                    return item;
                })
                .toList();

        return WrongAnswerPageView.from(items);
    }

    public WrongAnswerDetailView detail(SessionUser sessionUser, Long wrongAnswerId) {
        WrongAnswer wrongAnswer = getOwnedWrongAnswer(sessionUser, wrongAnswerId);

        PracticeProblem problem = practiceProblemMapper.findById(wrongAnswer.getProblemId())
                .orElseThrow(() -> new RuntimeException("문제를 찾을 수 없습니다."));

        return WrongAnswerDetailView.from(
                wrongAnswer.getWrongAnswerId(),
                problem.getProblemId(),
                problem.getQuestion(),
                problem.getAnswerText(),
                wrongAnswer.getWrongCount(),
                wrongAnswer.getReviewStatus(),
                wrongAnswer.getRetryBonusAwarded()
        );
    }

    @Transactional
    public void retry(SessionUser sessionUser,
                      WrongAnswerRetryForm form,
                      Long wrongAnswerId) {

        WrongAnswer wrongAnswer = getOwnedWrongAnswer(sessionUser, wrongAnswerId);

        PracticeProblem problem = practiceProblemMapper.findById(wrongAnswer.getProblemId())
                .orElseThrow(() -> new RuntimeException("문제를 찾을 수 없습니다."));

        boolean isCorrect = problem.getAnswerText().equals(form.getSubmittedAnswer());

        PracticeSubmission submission = new PracticeSubmission();
        submission.setSetAttemptId(wrongAnswer.getSetAttemptId());
        submission.setUserId(sessionUser.userId());
        submission.setProblemId(problem.getProblemId());
        submission.setSubmissionContext("WRONG_ANSWER_RETRY");
        submission.setSubmittedAnswer(form.getSubmittedAnswer());
        submission.setIsCorrect(isCorrect);
        submission.setIsSkipped(false);

        practiceSubmissionMapper.insertSubmission(submission);

        if (isCorrect) {
            wrongAnswerMapper.updateWrongAnswer(wrongAnswerId);
        }
    }

    @Transactional
    public void markReviewed(SessionUser sessionUser, Long wrongAnswerId) {
        getOwnedWrongAnswer(sessionUser, wrongAnswerId);
        wrongAnswerMapper.updateWrongAnswer(wrongAnswerId);
    }

    private WrongAnswer getOwnedWrongAnswer(SessionUser sessionUser, Long wrongAnswerId) {
        WrongAnswer wrongAnswer = wrongAnswerMapper.findByIdWrongAnswer(wrongAnswerId)
                .orElseThrow(() -> new RuntimeException("오답 기록을 찾을 수 없습니다."));

        if (!wrongAnswer.getUserId().equals(sessionUser.userId())) {
            throw new RuntimeException("본인의 오답 기록만 조회할 수 있습니다.");
        }

        return wrongAnswer;
    }
}
