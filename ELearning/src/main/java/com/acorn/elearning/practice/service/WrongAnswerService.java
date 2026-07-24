package com.acorn.elearning.practice.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.practice.form.WrongAnswerRetryForm;
import com.acorn.elearning.practice.mapper.PracticeProblemMapper;
import com.acorn.elearning.practice.mapper.PracticeSubmissionMapper;
import com.acorn.elearning.practice.mapper.ScoreEventMapper;
import com.acorn.elearning.practice.mapper.WrongAnswerMapper;
import com.acorn.elearning.practice.model.PracticeProblem;
import com.acorn.elearning.practice.model.PracticeSubmission;
import com.acorn.elearning.practice.model.WrongAnswer;
import com.acorn.elearning.practice.view.WrongAnswerDetailView;
import com.acorn.elearning.practice.view.WrongAnswerNote;
import com.acorn.elearning.practice.view.WrongAnswerPageView;
import com.acorn.elearning.practice.view.WrongAnswerSummaryView;
import com.acorn.elearning.security.SessionUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DuplicateKeyException;


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
    private final ScoreEventMapper scoreEventMapper;
    private final ScoreService scoreService;

    public WrongAnswerService(WrongAnswerMapper wrongAnswerMapper, PracticeProblemMapper practiceProblemMapper, PracticeSubmissionMapper practiceSubmissionMapper, ScoreEventMapper scoreEventMapper, ScoreService scoreService) {

        this.wrongAnswerMapper = wrongAnswerMapper;
        this.practiceProblemMapper = practiceProblemMapper;
        this.practiceSubmissionMapper = practiceSubmissionMapper;
        this.scoreEventMapper = scoreEventMapper;
        this.scoreService = scoreService;
    }

    // 오답을 데이터베이스에 기록
    @Transactional
    public void recordWrongAnswer(Long setAttemptId, Long userId, Long problemId, Long submissionId) {
        Optional<WrongAnswer> existing = wrongAnswerMapper.findByUserIdAndProblemId(userId, problemId);

        if (existing.isPresent()) {
            WrongAnswer wrongAnswer = existing.get();
            wrongAnswer.setLastSubmissionId(submissionId);
            wrongAnswer.setWrongCount(wrongAnswer.getWrongCount() + 1);
            wrongAnswer.setReviewStatus("PENDING");
            wrongAnswer.setRetryBonusAwarded(false);

            int updatedRows = wrongAnswerMapper.updateWrongAnswerOnNewMistake(wrongAnswer);
            if (updatedRows == 0) {
                throw new BusinessException(
                        ErrorCode.COMMON_INTERNAL_ERROR,
                        "오답노트 갱신에 실패했습니다."
                );
            }
            return;
        }
        WrongAnswer wrongAnswer = new WrongAnswer();
        wrongAnswer.setSetAttemptId(setAttemptId);
        wrongAnswer.setUserId(userId);
        wrongAnswer.setProblemId(problemId);
        wrongAnswer.setLastSubmissionId(submissionId);
        wrongAnswer.setWrongCount(1);
        wrongAnswer.setReviewStatus("PENDING");
        wrongAnswer.setRetryBonusAwarded(false);

        wrongAnswerMapper.insertWrongAnswer(wrongAnswer);
    }


    // 사용자 ID로 오답 목록 가져오기
    public List<WrongAnswer> getWrongAnswersByUserId(Long userId) {
        return wrongAnswerMapper.findAllWrongAnswersByUserId(userId);
    }


    //오답목록조회
    public WrongAnswerSummaryView summary(SessionUser sessionUser) {
        if (sessionUser == null || sessionUser.userId() == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }

        List<WrongAnswer> wrongAnswers =
                wrongAnswerMapper.findAllWrongAnswersByUserId(sessionUser.userId());

        int total = wrongAnswers.size();

        int solved = (int) wrongAnswers.stream()
                .filter(w -> "SOLVED".equals(w.getReviewStatus()))
                .count();

        int pending = total - solved;

        return WrongAnswerSummaryView.from(total, pending, solved);
    }

    //시그니처에 nodeId 추가
    public WrongAnswerPageView list(SessionUser sessionUser, Long nodeId, Long lessonId) {
        if (sessionUser == null || sessionUser.userId() == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }

        //nodeid용 분기 추가
            List<WrongAnswer> wrongAnswers;
        if (lessonId != null) {
            wrongAnswers = wrongAnswerMapper.findWrongAnswersByUserIdAndLessonId(
                    sessionUser.userId(),
                    lessonId
            );
        } else if (nodeId != null) {
                wrongAnswers = wrongAnswerMapper.findWrongAnswersByUserIdAndNodeId(
                        sessionUser.userId(),
                        nodeId
                );
            } else {
                wrongAnswers = wrongAnswerMapper.findAllWrongAnswersByUserId(sessionUser.userId());
            }

            List<Map<String, Object>> items = wrongAnswers.stream()
                    .map(w -> {
                        PracticeProblem problem = practiceProblemMapper.findById(w.getProblemId())
                                .orElseThrow(() -> new BusinessException(
                                        ErrorCode.COMMON_NOT_FOUND,
                                        "문제를 찾을 수 없습니다."));

                        Map<String, Object> item = new HashMap<>();
                        item.put("wrongAnswerId", w.getWrongAnswerId());
                        item.put("problemId", w.getProblemId());
                        item.put("lessonId", problem.getLessonId());
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
        PracticeProblem problem = getProblem(wrongAnswer);

        return WrongAnswerDetailView.from(
                wrongAnswer.getWrongAnswerId(),
                problem.getProblemId(),
                problem.getQuestion(),
                problem.getAnswerText(),
                problem.getExplanation(),
                problem.getLessonId(),
                problem.getNodeId(),
                wrongAnswer.getWrongCount(),
                wrongAnswer.getReviewStatus(),
                wrongAnswer.getRetryBonusAwarded()
        );
    }

    /**
     * 오답 상세를 Markdown 파일과 커뮤니티 초안에 공통으로 쓸 수 있는 형태로 만든다.
     * 소유권 검증은 상세 조회와 동일하게 이 메서드 안에서 수행한다.
     */
    public WrongAnswerNote note(SessionUser sessionUser, Long wrongAnswerId) {
        WrongAnswer wrongAnswer = getOwnedWrongAnswer(sessionUser, wrongAnswerId);
        PracticeProblem problem = getProblem(wrongAnswer);

        String question = normalizedText(problem.getQuestion(), "문제 내용이 없습니다.");
        String answer = normalizedText(problem.getAnswerText(), "등록된 정답이 없습니다.");
        String explanation = normalizedText(problem.getExplanation(), "등록된 해설이 없습니다.");
        String postTitle = "오답 복습 | " + summarize(question);

        String markdown = "# " + postTitle + "\n\n"
                + "> 오답노트에서 생성한 초안입니다. 학습한 내용과 느낀 점을 보태어 게시해 보세요.\n\n"
                + "- 문제 ID: `" + problem.getProblemId() + "`\n"
                + "- 오답 횟수: `" + wrongAnswer.getWrongCount() + "회`\n"
                + "- 복습 상태: `" + normalizedText(wrongAnswer.getReviewStatus(), "PENDING") + "`\n\n"
                + "## 문제\n"
                + toBlockQuote(question) + "\n\n"
                + "## 정답\n"
                + toBlockQuote(answer) + "\n\n"
                + "## 해설\n"
                + toBlockQuote(explanation) + "\n";

        return new WrongAnswerNote(
                wrongAnswer.getWrongAnswerId(),
                problem.getSubjectId(),
                "knowva-wrong-note-" + wrongAnswer.getWrongAnswerId() + ".md",
                postTitle,
                markdown
        );
    }

    @Transactional
    public boolean retry(SessionUser sessionUser,
                         WrongAnswerRetryForm form,
                         Long wrongAnswerId) {

        WrongAnswer wrongAnswer = getOwnedWrongAnswer(sessionUser, wrongAnswerId);

        PracticeProblem problem = practiceProblemMapper.findById(wrongAnswer.getProblemId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.COMMON_NOT_FOUND,
                        "문제를 찾을 수 없습니다."
                ));

        boolean isCorrect = normalizeAnswer(problem.getAnswerText())
                .equals(normalizeAnswer(form.getSubmittedAnswer()));

        Optional<PracticeSubmission> existingSubmission =
                practiceSubmissionMapper.findBySetAttemptIdAndProblemIdAndContext(
                        wrongAnswer.getSetAttemptId(),
                        problem.getProblemId(),
                        "WRONG_ANSWER_RETRY"
                );

        Long latestSubmissionId;

        if (existingSubmission.isPresent()) {
            PracticeSubmission submission = existingSubmission.get();
            submission.setSubmittedAnswer(form.getSubmittedAnswer());
            submission.setIsCorrect(isCorrect);
            submission.setIsSkipped(false);

            int updatedRows = practiceSubmissionMapper.updateSubmission(submission);
            if (updatedRows == 0) {
                throw new BusinessException(
                        ErrorCode.COMMON_INTERNAL_ERROR,
                        "다시 제출 수정에 실패했습니다."
                );
            }

            latestSubmissionId = submission.getSubmissionId();
        } else {
            PracticeSubmission submission = new PracticeSubmission();
            submission.setSetAttemptId(wrongAnswer.getSetAttemptId());
            submission.setUserId(sessionUser.userId());
            submission.setProblemId(problem.getProblemId());
            submission.setSubmissionContext("WRONG_ANSWER_RETRY");
            submission.setSubmittedAnswer(form.getSubmittedAnswer());
            submission.setIsCorrect(isCorrect);
            submission.setIsSkipped(false);

            practiceSubmissionMapper.insertSubmission(submission);
            latestSubmissionId = submission.getSubmissionId();
        }

        // 오답 재정답 시 +5
        if (isCorrect) {
            String retryIdempotencyKey = "WRONG_ANSWER_RETRY:" + wrongAnswer.getWrongAnswerId();

            wrongAnswer.setReviewStatus("SOLVED");
            wrongAnswer.setRetryBonusAwarded(true);
            wrongAnswer.setLastSubmissionId(latestSubmissionId);

            int updatedRows = wrongAnswerMapper.updateWrongAnswerOnNewMistake(wrongAnswer);
            if (updatedRows == 0) {
                throw new BusinessException(
                        ErrorCode.COMMON_INTERNAL_ERROR,
                        "오답 보너스 상태 업데이트에 실패했습니다."
                );
            }

            int existingScoreEventCount = scoreEventMapper.countByIdempotencyKey(retryIdempotencyKey);

            if (existingScoreEventCount == 0) {
                scoreService.giveScore(
                        sessionUser.userId(),
                        problem.getSubjectId(),
                        wrongAnswer.getWrongAnswerId(),
                        "WRONG_ANSWER_RETRY",
                        5,
                        "WRONG_ANSWER_RETRY_CORRECT",
                        retryIdempotencyKey
                );
            }
        }

        return isCorrect;
    }

    @Transactional
    public void markReviewed(SessionUser sessionUser, Long wrongAnswerId) {
        getOwnedWrongAnswer(sessionUser, wrongAnswerId);
        wrongAnswerMapper.markWrongAnswerSolved(wrongAnswerId);
    }

    private WrongAnswer getOwnedWrongAnswer(SessionUser sessionUser, Long wrongAnswerId) {
        if (sessionUser == null || sessionUser.userId() == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }

        WrongAnswer wrongAnswer = wrongAnswerMapper.findByIdWrongAnswer(wrongAnswerId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.COMMON_NOT_FOUND,
                        "오답 기록을 찾을 수 없습니다."
                ));

        if (!wrongAnswer.getUserId().equals(sessionUser.userId())) {
            throw new BusinessException(
                    ErrorCode.AUTH_FORBIDDEN,
                    "본인의 오답 기록만 조회할 수 있습니다."
            );
        }

        return wrongAnswer;
    }

    private PracticeProblem getProblem(WrongAnswer wrongAnswer) {
        return practiceProblemMapper.findById(wrongAnswer.getProblemId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.COMMON_NOT_FOUND,
                        "문제를 찾을 수 없습니다."));
    }

    private String normalizedText(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.replace("\r\n", "\n").replace('\r', '\n').trim();
    }

    private String summarize(String value) {
        String compact = value.replaceAll("\\s+", " ").trim();
        if (compact.length() <= 70) {
            return compact;
        }
        return compact.substring(0, 70).trim() + "…";
    }

    private String toBlockQuote(String value) {
        return value.lines()
                .map(line -> line.isBlank() ? ">" : "> " + line)
                .reduce((left, right) -> left + "\n" + right)
                .orElse("> ");
    }

    private String normalizeAnswer(String value) {
        return value == null ? "" : value.replaceAll("\\s+", "");
    }
}
