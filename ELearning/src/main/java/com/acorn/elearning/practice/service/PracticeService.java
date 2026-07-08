package com.acorn.elearning.practice.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.acorn.elearning.learning.mapper.LessonMapper;
import com.acorn.elearning.learning.model.CurriculumNode;
import com.acorn.elearning.learning.service.CurriculumService;
import com.acorn.elearning.practice.dto.response.PracticeAnswerResultResponse;
import com.acorn.elearning.practice.dto.response.PracticeSetResponse;
import com.acorn.elearning.practice.form.CreatePracticeSetForm;
import com.acorn.elearning.practice.form.PracticeAnswerForm;
import com.acorn.elearning.practice.form.PracticeSetCompleteForm;
import com.acorn.elearning.practice.mapper.PracticeSetAttemptMapper;
import com.acorn.elearning.practice.mapper.PracticeSubmissionMapper;
import com.acorn.elearning.practice.mapper.ProblemChoiceMapper;
import com.acorn.elearning.practice.model.PracticeProblem;
import com.acorn.elearning.practice.model.PracticeSetAttempt;
import com.acorn.elearning.practice.model.PracticeSubmission;
import com.acorn.elearning.practice.model.ProblemChoice;
import com.acorn.elearning.practice.view.PracticeSetView;
import com.acorn.elearning.learning.service.AttendanceService;
import com.acorn.elearning.learning.service.ProgressService;
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
    private final ProgressService progressService;
    private final AttendanceService attendanceService;
    private final ProblemChoiceMapper problemChoiceMapper;

    //다음단원이동
    private final CurriculumService curriculumService;
    private final LessonMapper lessonMapper;



    //생성자 주입
    public PracticeService(PracticeSetAttemptMapper practiceSetAttemptMapper, ProblemService problemService, WrongAnswerService wrongAnswerService, PracticeSubmissionMapper practiceSubmissionMapper, ScoreService scoreService, ProgressService progressService, AttendanceService attendanceService, ProblemChoiceMapper problemChoiceMapper, CurriculumService curriculumService, LessonMapper lessonMapper) {
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

            // 1. 세트 이력 조회
            PracticeSetAttempt attempt = practiceSetAttemptMapper.findByIdAttempt(setAttemptId)
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 세트입니다."));

            int correctCount = 0;

            // 2. 답안 채점 및 기록
            for (PracticeAnswerForm.SingleAnswer answerForm : answerList) {
                PracticeProblem problem = problemService.getProblem(answerForm.getProblemId());

                boolean isCorrect = normalizeAnswer(problem.getAnswerText())
                        .equals(normalizeAnswer(answerForm.getSubmittedAnswer()));

                PracticeSubmission submission = new PracticeSubmission();
                submission.setSetAttemptId(setAttemptId);
                submission.setUserId(user.userId());
                submission.setProblemId(answerForm.getProblemId());
                submission.setSubmissionContext("PRACTICE_SET");
                submission.setSubmittedAnswer(answerForm.getSubmittedAnswer());
                submission.setIsCorrect(isCorrect);
                submission.setIsSkipped(false);

                practiceSubmissionMapper.insertSubmission(submission);
                Long submissionId = submission.getSubmissionId();

                if (isCorrect) {
                    correctCount++;

                    // 3. 점수 처리
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
                throw new RuntimeException("세트 기록 업데이트 실패: " + setAttemptId);
            }

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

            // 세트 통과 시 학습 로드맵 진행률과 출석 기록
            if (Boolean.TRUE.equals(attempt.getPassed())) {
                progressService.markPracticePassed(attempt.getUserId(), attempt.getSubjectId(), attempt.getNodeId());
                attendanceService.recordAttendanceOnPracticePass(attempt.getUserId(), attempt.getSetAttemptId());

                //세트완료시 +50점
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

            // 2. 이동 경로 로직 (1-4단원: 다음단원 5단원: 테스트)
            boolean isTestStep = false;
            String nextPath = "/learning";

            if (Boolean.TRUE.equals(attempt.getPassed())) {
                List<CurriculumNode> planetNodes = curriculumService.getRoadmap(attempt.getSubjectId()).stream()
                        .filter(node -> "PLANET".equals(node.getNodeType()))
                        .toList();

                int currentIndex = -1;
                for (int i = 0; i < planetNodes.size(); i++) {
                    if (planetNodes.get(i).getNodeId().equals(attempt.getNodeId())) {
                        currentIndex = i;
                        break;
                    }
                }

                if (currentIndex >= 0 && currentIndex < planetNodes.size() - 1) {
                    CurriculumNode nextNode = planetNodes.get(currentIndex + 1);

                    Long nextLessonId = lessonMapper.findAll().stream()
                            .filter(lesson -> Boolean.TRUE.equals(lesson.getIsActive()))
                            .filter(lesson -> nextNode.getNodeId().equals(lesson.getNodeId()))
                            .sorted((a, b) -> {
                                int sortCompare = Integer.compare(a.getSortOrder(), b.getSortOrder());
                                if (sortCompare != 0) {
                                    return sortCompare;
                                }
                                return Long.compare(a.getLessonId(), b.getLessonId());
                            })
                            .map(lesson -> lesson.getLessonId())
                            .findFirst()
                            .orElse(null);

                    nextPath = (nextLessonId != null)
                            ? "/learning/lessons/" + nextLessonId
                            : "/learning";
                } else {
                    isTestStep = true;
                    nextPath = "/exams/coding-test";
                }
            }

            //테스트 이동 확인용
            /*
            isTestStep = true;
            nextPath = "/exams/coding-test";
            */
            Map<String, Object> data = Map.of(
                    "passed", attempt.getPassed(),
                    "nextPath", nextPath,
                    "isTestStep", isTestStep
            );

            // 4. 기존 응답 객체 반환
            return PracticeSetResponse.success(data);
        }

    private String normalizeAnswer(String value) {
        return value == null ? "" : value.replaceAll("\\s+", "");
    }

}
