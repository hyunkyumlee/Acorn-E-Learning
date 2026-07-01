package com.acorn.elearning.exam.controller;

import com.acorn.elearning.exam.dto.request.CreateExamRequest;
import com.acorn.elearning.exam.dto.request.SaveExamAnswerRequest;
import com.acorn.elearning.exam.dto.response.ExamProblemStepResponse;
import com.acorn.elearning.exam.dto.response.ExamResultResponse;
import com.acorn.elearning.exam.dto.response.ExamSessionResponse;
import com.acorn.elearning.exam.form.CreateExamForm;
import com.acorn.elearning.exam.form.ExamSubmitForm;
import com.acorn.elearning.exam.form.SaveExamAnswerForm;
import com.acorn.elearning.exam.service.AiExamService;
import com.acorn.elearning.security.SessionUser;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ExamController {
    private final AiExamService aiExamService;

    public ExamController(AiExamService aiExamService) {
        this.aiExamService = aiExamService;
    }

    @GetMapping("/exams/coding-test")
    public String codingTest(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            Model model
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("screen", "exam/coding-test");
        model.addAttribute("form", new CreateExamForm());
        model.addAttribute("eligibility", aiExamService.eligibility(sessionUser));
        return "exam/coding-test";
    }

    @PostMapping("/exams")
    public String create(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @Valid CreateExamForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("screen", "exam/coding-test");
            model.addAttribute("eligibility", aiExamService.eligibility(sessionUser));
            return "exam/coding-test";
        }
        ExamSessionResponse response = aiExamService.create(sessionUser, new CreateExamRequest(form.getSubjectId(), form.getLevelCode()));
        redirectAttributes.addAttribute("examId", response.examId());
        return "redirect:/exams/{examId}/problems/1";
    }

    @GetMapping("/exams/{examId}")
    public String detail(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long examId,
            Model model
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        ExamSessionResponse exam = aiExamService.detail(sessionUser, examId);
        if ("GRADED".equals(exam.status())) {
            return "redirect:/exams/{examId}/result";
        }
        return "redirect:/exams/{examId}/problems/1";
    }

    @GetMapping("/exams/{examId}/problems/{problemNo}")
    public String problem(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long examId,
            @PathVariable Integer problemNo,
            Model model
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        ExamSessionResponse exam = aiExamService.detail(sessionUser, examId);
        if ("GRADED".equals(exam.status())) {
            return "redirect:/exams/{examId}/result";
        }
        ExamProblemStepResponse step = ExamProblemStepResponse.from(exam, problemNo);
        model.addAttribute("screen", "exam/detail");
        model.addAttribute("exam", exam);
        model.addAttribute("step", step);
        return "exam/detail";
    }

    @PostMapping("/exams/{examId}/answers/{aiProblemId}")
    public String saveAnswer(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long examId,
            @PathVariable Long aiProblemId,
            @Valid SaveExamAnswerForm form
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        aiExamService.saveAnswer(sessionUser, examId, aiProblemId, new SaveExamAnswerRequest(form.getAnswerText()));
        return "redirect:/exams/{examId}/problems/1";
    }

    @PostMapping("/exams/{examId}/problems/{problemNo}/answers/{aiProblemId}")
    public String saveStepAnswer(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long examId,
            @PathVariable Integer problemNo,
            @PathVariable Long aiProblemId,
            @Valid SaveExamAnswerForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        redirectAttributes.addAttribute("examId", examId);
        redirectAttributes.addAttribute("problemNo", problemNo);
        if (bindingResult.hasErrors()) {
            return "redirect:/exams/{examId}/problems/{problemNo}";
        }
        ExamSessionResponse exam = aiExamService.saveAnswer(sessionUser, examId, aiProblemId, new SaveExamAnswerRequest(form.getAnswerText()));
        ExamProblemStepResponse step = ExamProblemStepResponse.from(exam, problemNo);
        if ("submit".equals(form.getMove()) && step.allAnswered()) {
            aiExamService.submit(sessionUser, examId);
            return "redirect:/exams/{examId}/result";
        }
        redirectAttributes.addAttribute("problemNo", targetProblemNo(step, form.getMove()));
        return "redirect:/exams/{examId}/problems/{problemNo}";
    }

    @PostMapping("/exams/{examId}/submit")
    public String submit(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long examId,
            @Valid ExamSubmitForm form
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        aiExamService.submit(sessionUser, examId);
        return "redirect:/exams/{examId}/result";
    }

    @PostMapping("/exams/{examId}/problems/{problemNo}/submit")
    public String submitStep(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long examId,
            @PathVariable Integer problemNo,
            @Valid ExamSubmitForm form
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        aiExamService.submit(sessionUser, examId);
        return "redirect:/exams/{examId}/result";
    }

    @PostMapping("/exams/{examId}/retry-execution")
    public String retryExecution(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long examId
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        aiExamService.retryExecution(sessionUser, examId);
        return "redirect:/exams/{examId}/result";
    }

    @GetMapping("/exams/{examId}/result")
    public String result(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long examId,
            Model model
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        ExamResultResponse result = aiExamService.result(sessionUser, examId);
        if (!"GRADED".equals(result.status())) {
            return "redirect:/exams/{examId}";
        }
        model.addAttribute("screen", "exam/result");
        model.addAttribute("result", result);
        return "exam/result";
    }

    private Integer targetProblemNo(ExamProblemStepResponse step, String move) {
        if ("next".equals(move) && step.nextProblemNo() != null) {
            return step.nextProblemNo();
        }
        if ("previous".equals(move) && step.previousProblemNo() != null) {
            return step.previousProblemNo();
        }
        return step.currentProblemNo();
    }
}
