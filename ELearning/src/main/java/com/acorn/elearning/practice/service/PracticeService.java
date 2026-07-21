package com.acorn.elearning.practice.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.learning.mapper.LessonMapper;
import com.acorn.elearning.learning.mapper.UserLessonProgressMapper;
import com.acorn.elearning.learning.service.CurriculumService;
import com.acorn.elearning.practice.dto.response.PracticeAnswerResultResponse;
import com.acorn.elearning.practice.dto.response.PracticeSetResponse;
import com.acorn.elearning.practice.form.CreatePracticeSetForm;
import com.acorn.elearning.practice.form.PracticeAnswerForm;
import com.acorn.elearning.practice.form.PracticeSetCompleteForm;
import com.acorn.elearning.practice.mapper.PracticeSetAttemptMapper;
import com.acorn.elearning.practice.mapper.PracticeSetItemMapper;
import com.acorn.elearning.practice.mapper.PracticeSubmissionMapper;
import com.acorn.elearning.practice.mapper.ProblemChoiceMapper;
import com.acorn.elearning.practice.model.*;
import com.acorn.elearning.practice.view.PracticeSetView;
import com.acorn.elearning.learning.service.AttendanceService;
import com.acorn.elearning.learning.service.ProgressService;
import com.acorn.elearning.security.SessionUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class PracticeService {

    private final PracticeSetAttemptMapper practiceSetAttemptMapper;
    private final PracticeSubmissionMapper practiceSubmissionMapper;
    private final ProblemService problemService;
    private final WrongAnswerService wrongAnswerService;
    private final ScoreService scoreService;
    private final ProgressService progressService;
    private final AttendanceService attendanceService;
    private final ProblemChoiceMapper problemChoiceMapper;

    //다음단원이동
    private final CurriculumService curriculumService;
    private final LessonMapper lessonMapper;
    //문제풀이 완료표시
    private final UserLessonProgressMapper userLessonProgressMapper;
    //문제풀이 순서저장
    private final PracticeSetItemMapper practiceSetItemMapper;



    //생성자 주입
    public PracticeService(PracticeSetAttemptMapper practiceSetAttemptMapper, ProblemService problemService, WrongAnswerService wrongAnswerService, PracticeSubmissionMapper practiceSubmissionMapper, ScoreService scoreService, ProgressService progressService, AttendanceService attendanceService, ProblemChoiceMapper problemChoiceMapper, CurriculumService curriculumService, LessonMapper lessonMapper, UserLessonProgressMapper userLessonProgressMapper, PracticeSetItemMapper practiceSetItemMapper) {
        this.practiceSetAttemptMapper = practiceSetAttemptMapper;
        this.problemService = problemService;
        this.wrongAnswerService = wrongAnswerService;
        this.practiceSubmissionMapper = practiceSubmissionMapper;
        this.scoreService = scoreService;
        this.progressService = progressService;
        this.attendanceService = attendanceService;
        this.problemChoiceMapper = problemChoiceMapper;
        this.curriculumService = curriculumService;
        this.lessonMapper = lessonMapper;

        this.userLessonProgressMapper = userLessonProgressMapper;
        this.practiceSetItemMapper = practiceSetItemMapper;
    }

    //문제조회
    @Transactional
    public PracticeSetView createPracticeSet(SessionUser user, CreatePracticeSetForm form) {

        // 1. 문제 조회
        if (form.getLessonId() == null) {
            throw new BusinessException(
                    ErrorCode.COMMON_VALIDATION_FAILED,
                    "lessonId가 없습니다."
            );
        }

        List<PracticeProblem> problems = problemService.getProblemsByLessonId(form.getLessonId());

        if (problems == null || problems.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.COMMON_NOT_FOUND,
                    "해당 lesson에 문제가 없습니다."
            );
        }


        // 2. practice_set_attempts 생성
        PracticeSetAttempt attempt = new PracticeSetAttempt();
        attempt.setUserId(user.userId());
        attempt.setSubjectId(form.getSubjectId()); //과목
        attempt.setNodeId(form.getNodeId()); //단원id -행성
        attempt.setLessonId(form.getLessonId());//lessonid
        attempt.setTotalCount(problems.size()); // 10개
        attempt.setCorrectCount(0); // 시작할때 정답은 0개
        attempt.setStatus("IN_PROGRESS"); // 풀이 진행 중
        attempt.setPassed(false); // 아직 통과 못함

        // Mapper를 통해 DB에 Insert (insert 직후 attempt 객체 안에 자동 생성된 PK값이 담김)
        practiceSetAttemptMapper.insertAttempt(attempt);
        //문제순서 저장
        for (int i = 0; i < problems.size(); i++) {
            PracticeProblem problem = problems.get(i);

            PracticeSetItem item = new PracticeSetItem();
            item.setSetAttemptId(attempt.getSetAttemptId());
            item.setProblemId(problem.getProblemId());
            item.setSortOrder(i + 1);

            practiceSetItemMapper.insertSetItem(item);
        }

        // 3. 문제별 선택지 조회
        // 객관식 문제는 choices를 화면에 넘기고, 정답(answerText/isCorrect)은 넘기지 않음
        Map<Long, List<ProblemChoice>> choiceMap = problems.stream()
                .collect(Collectors.toMap(
                        PracticeProblem::getProblemId,
                        problem -> problemChoiceMapper.findByProblemId(problem.getProblemId())
                ));

        return PracticeSetView.from(
                attempt.getSetAttemptId(),
                problems,
                choiceMap
        );
    }

        //submission 관련 business logic
        @Transactional
        public PracticeAnswerResultResponse submitAnswers(SessionUser user, PracticeSetCompleteForm completeForm) {
            Long setAttemptId = completeForm.getSetAttemptId();
            List<PracticeAnswerForm.SingleAnswer> answerList = completeForm.getAnswers();

            PracticeSetAttempt attempt = requireOwnedAttempt(user, setAttemptId);
            if (!"IN_PROGRESS".equals(attempt.getStatus())) {
                throw new BusinessException(
                        ErrorCode.COMMON_IDEMPOTENCY_CONFLICT,
                        "이미 제출 또는 완료된 연습 세트입니다."
                );
            }

            if (answerList == null || answerList.size() != attempt.getTotalCount()) {
                throw new BusinessException(
                        ErrorCode.COMMON_VALIDATION_FAILED,
                        "모든 문제의 답안을 제출해야 합니다."
                );
            }

            int correctCount = 0;

            // 2. 답안 채점 및 기록--결과화면 값 추가
            for (PracticeAnswerForm.SingleAnswer answerForm : answerList) {
                PracticeProblem problem = problemService.getProblem(answerForm.getProblemId());

                boolean isSkipped = "__SKIPPED__".equals(answerForm.getSubmittedAnswer());
                boolean isCorrect = !isSkipped && normalizeAnswer(problem.getAnswerText())
                        .equals(normalizeAnswer(answerForm.getSubmittedAnswer()));

                // 저장된 문제순서 불러오기
                PracticeSetItem setItem = practiceSetItemMapper.findBySetAttemptIdAndProblemId(
                        setAttemptId,
                        answerForm.getProblemId()
                );

                if (setItem == null) {
                    throw new BusinessException(
                            ErrorCode.COMMON_NOT_FOUND,
                            "문제 제출 대상을 찾을 수 없습니다."
                    );
                }

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
                Long submissionId = submission.getSubmissionId();

                //점수처리&결과계산
                if (isSkipped) {
                    continue;
                }

                if (isCorrect) {
                    correctCount++;

                    scoreService.giveScore(
                            user.userId(),
                            attempt.getSubjectId(),
                            submissionId,
                            "PRACTICE_SUBMISSION",
                            10,
                            "PRACTICE_CORRECT",
                            "PRACTICE_CORRECT:" + submissionId
                    );
                } else {
                    wrongAnswerService.recordWrongAnswer(
                            setAttemptId,
                            user.userId(),
                            answerForm.getProblemId(),
                            submissionId
                    );
                }
            }

            // 4. 세트 완료 처리
            attempt.setCorrectCount(correctCount);
            attempt.setStatus("COMPLETED");
            attempt.setPassed(correctCount >= 7);

            int updatedRows = practiceSetAttemptMapper.updateAttempt(attempt);
            if (updatedRows == 0) {
                throw new BusinessException(
                        ErrorCode.COMMON_INTERNAL_ERROR,
                        "세트 기록 업데이트에 실패했습니다."
                );
            }

            return PracticeAnswerResultResponse.from(correctCount, answerList.size());
        }

        //다음단원이동여부 및 경로

        @Transactional
        public PracticeSetResponse completeSet(SessionUser user, Long setAttemptId) {
            PracticeSetAttempt attempt = requireOwnedAttempt(user, setAttemptId);

            if (!"COMPLETED".equals(attempt.getStatus())) {
                throw new BusinessException(
                        ErrorCode.COMMON_IDEMPOTENCY_CONFLICT,
                        "답안 제출이 완료된 연습 세트만 완료 처리할 수 있습니다."
                );
            }

            if (Boolean.TRUE.equals(attempt.getPassed())) {
                boolean firstCompletion = scoreService.giveScoreIfAbsent(
                        attempt.getUserId(),
                        attempt.getSubjectId(),
                        attempt.getSetAttemptId(),
                        "PRACTICE_SET",
                        50,
                        "PRACTICE_SET_PASS",
                        "PRACTICE_SET_PASS:" + attempt.getSetAttemptId()
                );

                if (firstCompletion) {
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
                }
            }

            // 다음단원 이동 경로
            boolean isTestStep = false;
            String nextPath = "/learning";

            String primaryPath;
            String primaryLabel;
            String secondaryPath;
            String secondaryLabel;

            if (!Boolean.TRUE.equals(attempt.getPassed())) {
                // 실패 시 next step 분기
                primaryPath = "/learning/practice?nodeId=" + attempt.getNodeId()
                        + "&lessonId=" + attempt.getLessonId();
                primaryLabel = "문제 다시풀기";

                secondaryPath = "/learning/nodes/" + attempt.getNodeId() + "/lessons";
                secondaryLabel = "레슨 목록으로";

                nextPath = primaryPath;
            } else {
                // 성공 시 next step 분기
                List<com.acorn.elearning.learning.model.Lesson> lessonsInNode = lessonMapper.findAll().stream()
                        .filter(lesson -> Boolean.TRUE.equals(lesson.getIsActive()))
                        .filter(lesson -> attempt.getNodeId().equals(lesson.getNodeId()))
                        .sorted((a, b) -> {
                            int sortCompare = Integer.compare(a.getSortOrder(), b.getSortOrder());
                            if (sortCompare != 0) {
                                return sortCompare;
                            }
                            return Long.compare(a.getLessonId(), b.getLessonId());
                        })
                        .toList();

                Long nextLessonId = null;

                for (int i = 0; i < lessonsInNode.size(); i++) {
                    if (lessonsInNode.get(i).getLessonId().equals(attempt.getLessonId())) {
                        if (i < lessonsInNode.size() - 1) {
                            nextLessonId = lessonsInNode.get(i + 1).getLessonId();
                        }
                        break;
                    }
                }

                if (nextLessonId != null) {
                    primaryPath = "/learning/lessons/" + nextLessonId;
                    primaryLabel = "다음 레슨으로";

                    secondaryPath = "/learning";
                    secondaryLabel = "학습 메인으로";

                    nextPath = primaryPath;
                } else {
                    primaryPath = "/learning";
                    primaryLabel = "학습 메인으로";

                    secondaryPath = "/learning/nodes/" + attempt.getNodeId() + "/lessons";
                    secondaryLabel = "레슨 목록으로";

                    nextPath = primaryPath;
                }
            }

            List<PracticeSubmission> submissions = practiceSubmissionMapper.findBySetAttemptId(setAttemptId);

            int skippedCount = (int) submissions.stream()
                    .filter(submission -> Boolean.TRUE.equals(submission.getIsSkipped()))
                    .count();

            int correctCount = attempt.getCorrectCount() == null ? 0 : attempt.getCorrectCount();
            int totalCount = attempt.getTotalCount() == null ? 0 : attempt.getTotalCount();
            int wrongCount = totalCount - correctCount - skippedCount;

            if (wrongCount < 0) {
                wrongCount = 0;
            }

            Map<String, Object> data = new java.util.HashMap<>();
            data.put("passed", attempt.getPassed());
            data.put("nextPath", nextPath);
            data.put("isTestStep", isTestStep);
            data.put("primaryPath", primaryPath);
            data.put("primaryLabel", primaryLabel);
            data.put("secondaryPath", secondaryPath);
            data.put("secondaryLabel", secondaryLabel);
            data.put("nodeId", attempt.getNodeId());
            data.put("lessonId", attempt.getLessonId());
            data.put("correctCount", correctCount);
            data.put("wrongCount", wrongCount);
            data.put("skippedCount", skippedCount);
            data.put("totalCount", totalCount);

            return PracticeSetResponse.success(data);
        }
    //답안사이 스페이스무시용
    private String normalizeAnswer(String value) {
        return value == null ? "" : value.replaceAll("\\s+", "");
    }


    private PracticeSetAttempt requireOwnedAttempt(SessionUser user, Long setAttemptId){
        PracticeSetAttempt attempt = practiceSetAttemptMapper.findByIdAttempt(setAttemptId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.COMMON_NOT_FOUND,
                        "연습 세트를 찾을 수 없습니다."
                ));

        if(!attempt.getUserId().equals(user.userId())){
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN);
        }

        return attempt;
    }
}
