package com.acorn.elearning.practice.controller;

import com.acorn.elearning.practice.dto.response.PracticeSetResponse;
import com.acorn.elearning.practice.dto.response.PracticeAnswerResultResponse;
import com.acorn.elearning.practice.form.CreatePracticeSetForm;
import com.acorn.elearning.practice.form.PracticeAnswerForm;
import com.acorn.elearning.practice.form.PracticeSetCompleteForm;
import com.acorn.elearning.practice.model.PracticeProblem;
import com.acorn.elearning.practice.service.PracticeService;
import com.acorn.elearning.practice.service.ProblemService;
import com.acorn.elearning.practice.view.PracticeSetView;
import com.acorn.elearning.security.SessionUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.acorn.elearning.learning.controller.LearningController;
import com.acorn.elearning.learning.mapper.CurriculumNodeMapper;
import com.acorn.elearning.learning.mapper.LessonMapper;
import com.acorn.elearning.learning.model.CurriculumNode;
import com.acorn.elearning.learning.model.Lesson;

import java.util.Optional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class PracticeController {

    private static final String PRACTICE_VIEW_SESSION_KEY = "practiceView";
    private static final String PRACTICE_ANSWERS_SESSION_KEY = "practiceAnswers";
    private static final String PRACTICE_COMPLETE_RESULT_SESSION_KEY = "practiceCompleteResult";
    private static final String PRACTICE_LESSON_ID_SESSION_KEY = "practiceLessonId";

    private final ProblemService problemService;
    private final PracticeService practiceService;
    private final LessonMapper lessonMapper;
    private final CurriculumNodeMapper curriculumNodeMapper;

    public PracticeController(ProblemService problemService, PracticeService practiceService, LessonMapper lessonMapper, CurriculumNodeMapper curriculumNodeMapper) {
        this.problemService = problemService;
        this.practiceService = practiceService;
        this.lessonMapper = lessonMapper;
        this.curriculumNodeMapper = curriculumNodeMapper;
    }

    @GetMapping("/learning/practice")
    public String index(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @RequestParam(required = false) Long nodeId,
            @RequestParam(required = false) Long lessonId,
            @RequestParam(defaultValue = "BRONZE") String difficultyCode,
            HttpSession session,
            Model model) {

        if (sessionUser == null) {
            return "redirect:/login";
        }

        // subjectId는 learning 화면의 현재 선택 과목을 세션에서 사용.
       Long subjectId = (Long) session.getAttribute(LearningController.SESSION_LEARNING_SUBJECT_ID);

        if (subjectId == null) {
            subjectId = 1L;
        }

        PracticeSetResponse completeResult =
                (PracticeSetResponse) session.getAttribute(PRACTICE_COMPLETE_RESULT_SESSION_KEY);

        model.addAttribute("subjectId", subjectId);
        model.addAttribute("nodeId", nodeId);
        model.addAttribute("lessonId", lessonId);
        model.addAttribute("difficultyCode", difficultyCode);

        Long practiceLessonId = (Long) session.getAttribute(PRACTICE_LESSON_ID_SESSION_KEY);

        Lesson lesson = null;
        CurriculumNode node = null;

        if (practiceLessonId != null) {
            lesson = lessonMapper.findById(practiceLessonId).orElse(null);

            if (lesson != null && lesson.getNodeId() != null) {
                node = curriculumNodeMapper.findById(lesson.getNodeId()).orElse(null);
            }
        }

        model.addAttribute("lesson", lesson);
        model.addAttribute("node", node);

        if (completeResult != null) {
            model.addAttribute("completeResult", completeResult);
            model.addAttribute("completed", true);
            model.addAttribute("problem", null);
            model.addAttribute("screen", "learning/practice");
            session.removeAttribute(PRACTICE_COMPLETE_RESULT_SESSION_KEY);
            return "learning/practice";
        }

        model.addAttribute("completed", false);
        model.addAttribute("problem", null);
        model.addAttribute("screen", "learning/practice");
        return "learning/practice";
    }

    @PostMapping("/learning/practice/sets")
    public String createSet(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @Validated CreatePracticeSetForm form,
            BindingResult bindingResult,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "learning/practice";
        }

        if (sessionUser == null) {
            return "redirect:/login";
        }

        PracticeSetView view = practiceService.createPracticeSet(sessionUser, form);

        List<Map<String, Object>> problems = getProblems(view);
        if (problems.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "출제된 문제가 없습니다.");
            return "redirect:/learning/practice";
        }

        Long firstProblemId = toLong(problems.get(0).get("problemId"));

        session.setAttribute(PRACTICE_VIEW_SESSION_KEY, view);
        session.setAttribute(PRACTICE_ANSWERS_SESSION_KEY, new ArrayList<PracticeAnswerForm.SingleAnswer>());
        session.setAttribute(PRACTICE_LESSON_ID_SESSION_KEY, form.getLessonId());
        session.removeAttribute(PRACTICE_COMPLETE_RESULT_SESSION_KEY);

        redirectAttributes.addAttribute("setAttemptId", view.attributes().get("setAttemptId"));
        redirectAttributes.addAttribute("index", 0);

        return "redirect:/learning/practice/problems/" + firstProblemId;
    }

    @GetMapping("/learning/practice/problems/{problemId}")
    public String problemDetail(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long problemId,
            @RequestParam Long setAttemptId,
            @RequestParam(defaultValue = "0") int index,
            Model model,
            HttpSession session) {

        if (sessionUser == null) {
            return "redirect:/login";
        }

        PracticeSetView view = (PracticeSetView) session.getAttribute(PRACTICE_VIEW_SESSION_KEY);
        if (view == null) {
            return "redirect:/learning/practice";
        }

        List<Map<String, Object>> problems = getProblems(view);
        if (problems == null || problems.isEmpty()) {
            return "redirect:/learning/practice";
        }

        if (index < 0 || index >= problems.size()) {
            index = 0;
        }

        Map<String, Object> currentProblem = problems.get(index);

        List<PracticeAnswerForm.SingleAnswer> answers = getAnswerSessionList(session);
        PracticeAnswerForm.SingleAnswer currentAnswer = index < answers.size() ? answers.get(index) : null;

        PracticeAnswerResultResponse flashResult =
                (PracticeAnswerResultResponse) model.asMap().get("lastResult");

        boolean isFirstProblem = index == 0;
        boolean isLastProblem = index == problems.size() - 1;

        Long prevProblemId = null;
        if (!isFirstProblem) {
            prevProblemId = toLong(problems.get(index - 1).get("problemId"));
        }

        Long nextProblemId = null;
        if (!isLastProblem) {
            nextProblemId = toLong(problems.get(index + 1).get("problemId"));
        }

        boolean alreadyProcessed = currentAnswer != null;
        boolean skipped = currentAnswer != null && "__SKIPPED__".equals(currentAnswer.getSubmittedAnswer());
        boolean canGoNext = alreadyProcessed;

        PracticeAnswerResultResponse restoredResult = null;

        if (flashResult != null) {
            restoredResult = flashResult;
        } else if (currentAnswer != null && !skipped) {
            PracticeProblem problem = problemService.getProblem(problemId);
            boolean correct = normalizeAnswer(problem.getAnswerText())
                    .equals(normalizeAnswer(currentAnswer.getSubmittedAnswer()));

            restoredResult = new PracticeAnswerResultResponse(
                    "SUCCESS",
                    Map.of(
                            "problemId", problemId,
                            "submittedAnswer", currentAnswer.getSubmittedAnswer(),
                            "correct", correct,
                            "correctAnswer", problem.getAnswerText(),
                            "explanation", "",
                            "scoreDelta", 0,
                            "wrongAnswerUpdated", !correct
                    )
            );
        }

        boolean answered = restoredResult != null;

        Long lessonId = (Long) session.getAttribute(PRACTICE_LESSON_ID_SESSION_KEY);

        Lesson lesson = null;
        CurriculumNode node = null;

        if (lessonId != null) {
            lesson = lessonMapper.findById(lessonId).orElse(null);

            if (lesson != null && lesson.getNodeId() != null) {
                node = curriculumNodeMapper.findById(lesson.getNodeId()).orElse(null);
            }
        }

        model.addAttribute("lesson", lesson);
        model.addAttribute("node", node);

        model.addAttribute("view", view);
        model.addAttribute("problem", currentProblem);
        model.addAttribute("setAttemptId", setAttemptId);
        model.addAttribute("currentIndex", index);
        model.addAttribute("totalCount", problems.size());

        model.addAttribute("isFirstProblem", isFirstProblem);
        model.addAttribute("isLastProblem", isLastProblem);
        model.addAttribute("prevProblemId", prevProblemId);
        model.addAttribute("nextProblemId", nextProblemId);
        model.addAttribute("canGoNext", canGoNext);
        model.addAttribute("skipped", skipped);

        model.addAttribute("lastResult", restoredResult);
        model.addAttribute("answered", answered);
        model.addAttribute("alreadyProcessed", alreadyProcessed);

        model.addAttribute("showFinalSkipConfirm",
                Boolean.TRUE.equals(model.asMap().get("showFinalSkipConfirm")));

        model.addAttribute("completed", false);
        model.addAttribute("screen", "learning/practice");

        return "learning/practice";
    }

    @PostMapping("/learning/practice/sets/{setAttemptId}/answers")
    public String submitAnswer(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long setAttemptId,
            @RequestParam Long problemId,
            @RequestParam(defaultValue = "0") int index,
            @RequestParam String submittedAnswer,
            RedirectAttributes redirectAttributes,
            HttpSession session
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }

        PracticeSetView view = (PracticeSetView) session.getAttribute(PRACTICE_VIEW_SESSION_KEY);
        List<PracticeAnswerForm.SingleAnswer> answers = getAnswerSessionList(session);

        if (view == null) {
            return "redirect:/learning/practice";
        }

        PracticeProblem problem = problemService.getProblem(problemId);
        boolean correct = normalizeAnswer(problem.getAnswerText())
                .equals(normalizeAnswer(submittedAnswer));

        PracticeAnswerForm.SingleAnswer answer = new PracticeAnswerForm.SingleAnswer();
        answer.setProblemId(problemId);
        answer.setSubmittedAnswer(submittedAnswer);

        setAnswerAtIndex(answers, index, answer);
        session.setAttribute(PRACTICE_ANSWERS_SESSION_KEY, answers);

        PracticeAnswerResultResponse result = new PracticeAnswerResultResponse(
                "SUCCESS",
                Map.of(
                        "problemId", problemId,
                        "submittedAnswer", submittedAnswer,
                        "correct", correct,
                        "correctAnswer", problem.getAnswerText(),
                        "explanation", problem.getExplanation(),
                        "scoreDelta", 0,
                        "wrongAnswerUpdated", !correct
                )
        );

        redirectAttributes.addFlashAttribute("lastResult", result);
        redirectAttributes.addAttribute("setAttemptId", setAttemptId);
        redirectAttributes.addAttribute("index", index);

        return "redirect:/learning/practice/problems/" + problemId;
    }

    @PostMapping("/learning/practice/sets/{setAttemptId}/complete")
    public String completeSet(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long setAttemptId,
            RedirectAttributes redirectAttributes,
            HttpSession session
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }

        List<PracticeAnswerForm.SingleAnswer> answers = getAnswerSessionList(session);
        if (answers.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "제출한 답안이 없습니다.");
            return "redirect:/learning/practice";
        }

        //건너뛰기 제출 포함 수정
        PracticeSetView view = (PracticeSetView) session.getAttribute(PRACTICE_VIEW_SESSION_KEY);
        if (view == null) {
            return "redirect:/learning/practice";
        }

        int totalProblemCount = getProblems(view).size();
        if (answers.size() < totalProblemCount) {
            redirectAttributes.addFlashAttribute("message", "아직 처리되지 않은 문제가 있습니다.");
            Long firstProblemId = toLong(getProblems(view).get(answers.size()).get("problemId"));
            redirectAttributes.addAttribute("setAttemptId", setAttemptId);
            redirectAttributes.addAttribute("index", answers.size());
            return "redirect:/learning/practice/problems/" + firstProblemId;
        }

        PracticeSetCompleteForm completeForm = new PracticeSetCompleteForm();
        completeForm.setSetAttemptId(setAttemptId);
        completeForm.setAnswers(answers);
        completeForm.setIdempotencyToken(
                "PRACTICE_COMPLETE:" + sessionUser.userId() + ":" + setAttemptId
        );

        practiceService.submitAnswers(sessionUser, completeForm);
        PracticeSetResponse completeResult = practiceService.completeSet(sessionUser, setAttemptId);

        session.removeAttribute(PRACTICE_VIEW_SESSION_KEY);
        session.removeAttribute(PRACTICE_ANSWERS_SESSION_KEY);
        session.setAttribute(PRACTICE_COMPLETE_RESULT_SESSION_KEY, completeResult);

        return "redirect:/learning/practice";
    }

    //건너뛰기 관련 추가 매서드

    @PostMapping("/learning/practice/sets/{setAttemptId}/skip")
    public String skipAnswer(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long setAttemptId,
            @RequestParam Long problemId,
            @RequestParam(defaultValue = "0") int index,
            @RequestParam(defaultValue = "false") boolean confirmFinalSkip,
            RedirectAttributes redirectAttributes,
            HttpSession session
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }

        PracticeSetView view = (PracticeSetView) session.getAttribute(PRACTICE_VIEW_SESSION_KEY);
        List<Map<String, Object>> problems = view == null ? null : getProblems(view);
        List<PracticeAnswerForm.SingleAnswer> answers = getAnswerSessionList(session);

        if (view == null || problems == null || problems.isEmpty()) {
            return "redirect:/learning/practice";
        }

        boolean isLastProblem = index == problems.size() - 1;

        if (isLastProblem && !confirmFinalSkip) {
            redirectAttributes.addFlashAttribute("message", "이 문제를 건너뛴 채 최종 제출하시겠습니까?");
            redirectAttributes.addFlashAttribute("showFinalSkipConfirm", true);
            redirectAttributes.addAttribute("setAttemptId", setAttemptId);
            redirectAttributes.addAttribute("index", index);
            return "redirect:/learning/practice/problems/" + problemId;
        }

        PracticeAnswerForm.SingleAnswer skippedAnswer = new PracticeAnswerForm.SingleAnswer();
        skippedAnswer.setProblemId(problemId);
        skippedAnswer.setSubmittedAnswer("__SKIPPED__");

        setAnswerAtIndex(answers, index, skippedAnswer);
        session.setAttribute(PRACTICE_ANSWERS_SESSION_KEY, answers);

        if (isLastProblem && confirmFinalSkip) {
            PracticeSetCompleteForm completeForm = new PracticeSetCompleteForm();
            completeForm.setSetAttemptId(setAttemptId);
            completeForm.setAnswers(answers);
            completeForm.setIdempotencyToken(
                    "PRACTICE_COMPLETE:" + sessionUser.userId() + ":" + setAttemptId
            );

            practiceService.submitAnswers(sessionUser, completeForm);
            PracticeSetResponse completeResult = practiceService.completeSet(sessionUser, setAttemptId);

            session.removeAttribute(PRACTICE_VIEW_SESSION_KEY);
            session.removeAttribute(PRACTICE_ANSWERS_SESSION_KEY);
            session.setAttribute(PRACTICE_COMPLETE_RESULT_SESSION_KEY, completeResult);

            return "redirect:/learning/practice";
        }

        Long nextProblemId = toLong(problems.get(index + 1).get("problemId"));

        redirectAttributes.addAttribute("setAttemptId", setAttemptId);
        redirectAttributes.addAttribute("index", index + 1);

        return "redirect:/learning/practice/problems/" + nextProblemId;
    }

    @GetMapping("/learning/practice/finalize/{setAttemptId}")
    public String finalizePractice(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long setAttemptId,
            HttpSession session
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }

        return "redirect:/learning/practice/sets/" + setAttemptId + "/complete-ready";
    }

    @GetMapping("/learning/practice/sets/{setAttemptId}/complete-ready")
    public String completeReady(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long setAttemptId,
            Model model,
            HttpSession session
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }

        PracticeSetView view = (PracticeSetView) session.getAttribute(PRACTICE_VIEW_SESSION_KEY);
        if (view == null) {
            return "redirect:/learning/practice";
        }

        List<Map<String, Object>> problems = getProblems(view);
        if (problems == null || problems.isEmpty()) {
            return "redirect:/learning/practice";
        }

        int lastIndex = problems.size() - 1;
        Map<String, Object> lastProblem = problems.get(lastIndex);

        model.addAttribute("view", view);
        model.addAttribute("problem", lastProblem);
        model.addAttribute("setAttemptId", setAttemptId);
        model.addAttribute("currentIndex", lastIndex);
        model.addAttribute("totalCount", problems.size());

        model.addAttribute("isFirstProblem", lastIndex == 0);
        model.addAttribute("isLastProblem", true);
        model.addAttribute("prevProblemId", lastIndex > 0 ? toLong(problems.get(lastIndex - 1).get("problemId")) : null);
        model.addAttribute("nextProblemId", null);
        model.addAttribute("canGoNext", false);
        model.addAttribute("skipped", true);

        model.addAttribute("lastResult", null);
        model.addAttribute("answered", false);
        model.addAttribute("alreadyProcessed", true);
        model.addAttribute("showFinalSkipConfirm", true);

        model.addAttribute("completed", false);
        model.addAttribute("screen", "learning/practice");

        return "learning/practice";
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getProblems(PracticeSetView view) {
        return (List<Map<String, Object>>) view.attributes().get("problems");
    }

    @SuppressWarnings("unchecked")
    private List<PracticeAnswerForm.SingleAnswer> getAnswerSessionList(HttpSession session) {
        List<PracticeAnswerForm.SingleAnswer> answers =
                (List<PracticeAnswerForm.SingleAnswer>) session.getAttribute(PRACTICE_ANSWERS_SESSION_KEY);

        if (answers == null) {
            answers = new ArrayList<>();
            session.setAttribute(PRACTICE_ANSWERS_SESSION_KEY, answers);
        }

        return answers;
    }

    private Long toLong(Object value) {
        if (value instanceof Long longValue) {
            return longValue;
        }
        if (value instanceof Integer intValue) {
            return intValue.longValue();
        }
        if (value instanceof Number numberValue) {
            return numberValue.longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }

    //건너뛰기 관련 추가
    private void setAnswerAtIndex(List<PracticeAnswerForm.SingleAnswer> answers,
                                  int index,
                                  PracticeAnswerForm.SingleAnswer answer) {
        while (answers.size() <= index) {
            answers.add(null);
        }
        answers.set(index, answer);
    }

    private String normalizeAnswer(String value) {
        return value == null ? "" : value.replaceAll("\\s+", "");
    }
}
