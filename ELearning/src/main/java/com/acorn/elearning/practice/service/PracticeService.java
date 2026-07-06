package com.acorn.elearning.practice.service;

import java.util.List;
import java.util.Map;

import com.acorn.elearning.practice.dto.response.PracticeAnswerResultResponse;
import com.acorn.elearning.practice.dto.response.PracticeSetResponse;
import com.acorn.elearning.practice.form.CreatePracticeSetForm;
import com.acorn.elearning.practice.form.PracticeAnswerForm;
import com.acorn.elearning.practice.form.PracticeSetCompleteForm;
import com.acorn.elearning.practice.mapper.PracticeProblemMapper;
import com.acorn.elearning.practice.mapper.PracticeSetAttemptMapper;
import com.acorn.elearning.practice.mapper.PracticeSubmissionMapper;
import com.acorn.elearning.practice.model.PracticeProblem;
import com.acorn.elearning.practice.model.PracticeSetAttempt;
import com.acorn.elearning.practice.model.PracticeSubmission;
import com.acorn.elearning.practice.view.PracticeSetView;
import com.acorn.elearning.security.SessionUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PracticeService {
   /* public Map<String, Object> stub(String action) {
        // TODO 구현 예시입니다. 실제 parameter와 return DTO로 method signature를 교체하세요.
        // List<PracticeProblem> problems = practiceProblemMapper.findAvailable(userId, subjectId);
        // PracticeSetAttempt attempt = PracticeSetAttempt.start(userId, problems);
        // practiceSetAttemptMapper.insert(attempt);
        // return Map.of("attempt", PracticeSetResponse.from(attempt, problems));
        return Map.of("action", action, "status", "SKELETON");
    }
    */
    private final PracticeSetAttemptMapper practiceSetAttemptMapper;
    private final PracticeSubmissionMapper practiceSubmissionMapper;
    private final ProblemService problemService;
    private final WrongAnswerService wrongAnswerService;
    private final ScoreService scoreService;


    //생성자 주입
    public PracticeService(PracticeSetAttemptMapper practiceSetAttemptMapper, ProblemService problemService, WrongAnswerService wrongAnswerService, PracticeSubmissionMapper practiceSubmissionMapper, ScoreService scoreService) {
        this.practiceSetAttemptMapper = practiceSetAttemptMapper;
        this.problemService = problemService;
        this.wrongAnswerService = wrongAnswerService;
        this.practiceSubmissionMapper = practiceSubmissionMapper;
        this.scoreService = scoreService;
    }

    //문제조회
    @Transactional
    public PracticeSetView createPracticeSet(SessionUser user, CreatePracticeSetForm form) {

        // 1. 문제 조회 ProblemService 위임
        List<PracticeProblem> problems = problemService.getProblems(
                form.getSubjectId(),
                form.getDifficultyCode()
            );


        // 2. practice_set_attempts 생성
        PracticeSetAttempt attempt = new PracticeSetAttempt();
        attempt.setUserId(user.userId());
        attempt.setSubjectId(form.getSubjectId()); //🔆과목
        attempt.setNodeId(form.getNodeId());
        attempt.setTotalCount(problems.size()); // 10개
        attempt.setCorrectCount(0); // 시작할때 정답은 0개
        attempt.setStatus("IN_PROGRESS"); // 풀이 진행 중
        attempt.setPassed(false); // 아직 통과 못함

        // Mapper를 통해 DB에 Insert (insert 직후 attempt 객체 안에 자동 생성된 PK값이 담김)
        practiceSetAttemptMapper.insertAttempt(attempt);

        // 3. 문제와 선택지를 화면용 ViewModel로 반환
        // (화면에 넘길 때 정답(answerText)은 숨기고 넘기기)
        return PracticeSetView.from(attempt.getSetAttemptId(), problems);
    }

        //submission 관련 business logic
        @Transactional
        public PracticeAnswerResultResponse submitAnswers(SessionUser user, PracticeSetCompleteForm completeForm) {
                Long setAttemptId = completeForm.getSetAttemptId();
                List<PracticeAnswerForm.SingleAnswer> answerList = completeForm.getAnswers();

        // 1. 세트 이력 조회 (DB에서 subjectId를 포함한 attempt 객체를 가져옴)
        PracticeSetAttempt attempt = practiceSetAttemptMapper.findByIdAttempt(setAttemptId)
               .orElseThrow(() -> new RuntimeException("존재하지 않는 세트입니다."));

        int correctCount = 0;

        // 2. 답안 채점 및 기록
        for (PracticeAnswerForm.SingleAnswer answerForm : answerList) {
            PracticeProblem problem = problemService.getProblem(answerForm.getProblemId());
            boolean isCorrect = problem.getAnswerText().equals(answerForm.getSubmittedAnswer());

            PracticeSubmission submission = new PracticeSubmission();
            submission.setSetAttemptId(setAttemptId);
            submission.setUserId(user.userId());
            submission.setProblemId(answerForm.getProblemId());
            submission.setSubmittedAnswer(answerForm.getSubmittedAnswer());
            submission.setIsCorrect(isCorrect);

            practiceSubmissionMapper.insertSubmission(submission);
            Long submissionId = submission.getSubmissionId();

            if (!isCorrect) {
                wrongAnswerService.recordWrongAnswer(setAttemptId, user.userId(), answerForm.getProblemId(), submissionId);
                } else {
                    correctCount++;
                }
            }

            // 3. 세트 완료 처리
            attempt.setCorrectCount(correctCount);
            attempt.setStatus("COMPLETED");
            attempt.setPassed(correctCount >= 7);

            int updatedRows = practiceSetAttemptMapper.updateAttempt(attempt);
            if (updatedRows == 0) {
                throw new RuntimeException("세트 기록 업데이트 실패: " + setAttemptId);
            }

            // 4. 점수 처리 (모델에서 바로 가져온 subjectId 사용)
            scoreService.giveScore(
                    user.userId(),
                    attempt.getSubjectId(), // 모델에서 직접 조회
                    10,
                    "PRACTICE_COMPLETE",
                    completeForm.getIdempotencyToken()
            );

            return PracticeAnswerResultResponse.from(correctCount, answerList.size());
        }

        //다음단원이동여부
        @Transactional
        public PracticeSetResponse completeSet(SessionUser user, Long setAttemptId) {
            PracticeSetAttempt attempt = practiceSetAttemptMapper.findByIdAttempt(setAttemptId)
                    .orElseThrow(() -> new RuntimeException("세트를 찾을 수 없습니다."));

            // 1. 상태 처리
            if (!"COMPLETED".equals(attempt.getStatus())) {
                attempt.setStatus("COMPLETED");
                practiceSetAttemptMapper.updateAttempt(attempt);
            }

            // 2. 이동 경로 로직
            boolean isTestStep = (attempt.getSubjectId() == 5);
            String nextPath = isTestStep ? "/learning/test/start" : "/learning/practice/next/" + (attempt.getSubjectId() + 1);

            // 3. 데이터를 Map에 담기
            Map<String, Object> data = Map.of(
                    "passed", attempt.getPassed(),
                    "nextPath", nextPath,
                    "isTestStep", isTestStep
            );

            // 4. 기존 응답 객체 반환
            return PracticeSetResponse.success(data);
        }

}
