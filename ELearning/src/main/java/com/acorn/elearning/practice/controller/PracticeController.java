package com.acorn.elearning.practice.controller;

import com.acorn.elearning.auth.service.SessionService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class PracticeController {

    private static final String PRACTICE_VIEW_SESSION_KEY = "practiceView";
    private static final String PRACTICE_ANSWERS_SESSION_KEY = "practiceAnswers";
    private static final String PRACTICE_COMPLETE_RESULT_SESSION_KEY = "practiceCompleteResult";

    private final ProblemService problemService;
    private final PracticeService practiceService;

    public PracticeController(ProblemService problemService, PracticeService practiceService) {
        this.problemService = problemService;
        this.practiceService = practiceService;
    }

    @GetMapping("/learning/practice")
    public String index(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            HttpSession session,
            Model model) {

        if (sessionUser == null) {
            return "redirect:/login";
        }

        PracticeSetResponse completeResult =
                (PracticeSetResponse) session.getAttribute(PRACTICE_COMPLETE_RESULT_SESSION_KEY);
        System.out.println("index completeResult = " + completeResult);


        if (completeResult != null) {
            model.addAttribute("completeResult", completeResult);
            model.addAttribute("completed", true);
            model.addAttribute("problem", null);
            model.addAttribute("screen", "learning/practice");
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

        PracticeAnswerResultResponse lastResult =
                (PracticeAnswerResultResponse) model.asMap().get("lastResult");

        boolean answered = lastResult != null;
        boolean isLastProblem = index == problems.size() - 1;

        Long nextProblemId = null;
        if (!isLastProblem) {
            nextProblemId = toLong(problems.get(index + 1).get("problemId"));
        }

        model.addAttribute("view", view);
        model.addAttribute("problem", currentProblem);
        model.addAttribute("setAttemptId", setAttemptId);
        model.addAttribute("currentIndex", index);
        model.addAttribute("totalCount", problems.size());
        model.addAttribute("isLastProblem", isLastProblem);
        model.addAttribute("nextProblemId", nextProblemId);
        model.addAttribute("lastResult", lastResult);
        model.addAttribute("answered", answered);
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
        boolean correct = problem.getAnswerText().equals(submittedAnswer);

        PracticeAnswerForm.SingleAnswer answer = new PracticeAnswerForm.SingleAnswer();
        answer.setProblemId(problemId);
        answer.setSubmittedAnswer(submittedAnswer);

        if (answers.size() > index) {
            answers.set(index, answer);
        } else {
            answers.add(answer);
        }

        session.setAttribute(PRACTICE_ANSWERS_SESSION_KEY, answers);

        PracticeAnswerResultResponse result = new PracticeAnswerResultResponse(
                "SUCCESS",
                Map.of(
                        "problemId", problemId,
                        "submittedAnswer", submittedAnswer,
                        "correct", correct,
                        "correctAnswer", problem.getAnswerText(),
                        "explanation", "",
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
            redirectAttributes.addFlashAttribute("message", "제출된 답안이 없습니다.");
            return "redirect:/learning/practice";
        }

        PracticeSetCompleteForm completeForm = new PracticeSetCompleteForm();
        completeForm.setSetAttemptId(setAttemptId);
        completeForm.setAnswers(answers);
        completeForm.setIdempotencyToken(
                "PRACTICE_COMPLETE:" + sessionUser.userId() + ":" + setAttemptId
        );

        practiceService.submitAnswers(sessionUser, completeForm);
        PracticeSetResponse completeResult = practiceService.completeSet(sessionUser, setAttemptId);
        System.out.println("completeSet called: " + setAttemptId);
        System.out.println("completeResult = " + completeResult);

        session.removeAttribute(PRACTICE_VIEW_SESSION_KEY);
        session.removeAttribute(PRACTICE_ANSWERS_SESSION_KEY);
        session.setAttribute(PRACTICE_COMPLETE_RESULT_SESSION_KEY, completeResult);

        return "redirect:/learning/practice";
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
}
