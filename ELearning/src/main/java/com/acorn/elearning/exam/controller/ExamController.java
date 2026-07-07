package com.acorn.elearning.exam.controller;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.idempotency.IdempotencyTokenService;
import com.acorn.elearning.exam.dto.request.CreateExamRequest;
import com.acorn.elearning.exam.dto.request.SaveExamAnswerRequest;
import com.acorn.elearning.exam.dto.response.ExamProblemStepResponse;
import com.acorn.elearning.exam.dto.response.ExamResultResponse;
import com.acorn.elearning.exam.dto.response.ExamSessionResponse;
import com.acorn.elearning.exam.form.CreateExamForm;
import com.acorn.elearning.exam.form.ExamSubmitForm;
import com.acorn.elearning.exam.form.SaveExamAnswerForm;
import com.acorn.elearning.exam.service.AiExamService;
import com.acorn.elearning.learning.mapper.SubjectMapper;
import com.acorn.elearning.learning.model.Subject;
import com.acorn.elearning.security.SessionUser;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class ExamController {
    private static final String CREATE_FORM_TYPE = "EXAM_CREATE";
    private static final String ANSWER_FORM_TYPE = "EXAM_ANSWER";
    private static final List<String> LEVEL_CODES = List.of("BRONZE", "SILVER", "GOLD");

    private final AiExamService aiExamService;
    private final IdempotencyTokenService idempotencyTokenService;
    private final SubjectMapper subjectMapper;

    public ExamController(AiExamService aiExamService, IdempotencyTokenService idempotencyTokenService, SubjectMapper subjectMapper) {
        this.aiExamService = aiExamService;
        this.idempotencyTokenService = idempotencyTokenService;
        this.subjectMapper = subjectMapper;
    }

    @GetMapping("/exams/coding-test")
    public String codingTest(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @RequestParam(name = "subjectId", required = false) Long subjectId,
            @RequestParam(name = "levelCode", required = false) String levelCode,
            HttpSession httpSession,
            Model model
    ) {
        String redirect = learnerRedirect(sessionUser);
        if (!redirect.isBlank()) { return redirect; }
        CreateExamForm form = new CreateExamForm();
        form.setSubjectId(subjectId);
        form.setLevelCode(levelCode);
        form.setIdempotencyToken(idempotencyTokenService.issue(CREATE_FORM_TYPE, httpSession, sessionUser.userId()).token());
        prepareCodingTestModel(sessionUser, model, form);
        return "exam/coding-test";
    }

    @GetMapping("/exams/coding-test/dev")
    public String codingTestDev(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @RequestParam(name = "subjectId", required = false) Long subjectId,
            @RequestParam(name = "levelCode", required = false) String levelCode,
            HttpSession httpSession,
            Model model
    ) {
        String redirect = learnerRedirect(sessionUser);
        if (!redirect.isBlank()) { return redirect; }
        CreateExamForm form = new CreateExamForm();
        form.setSubjectId(subjectId == null ? defaultSubjectId() : subjectId);
        form.setLevelCode(defaultLevelCode(levelCode));
        form.setIdempotencyToken(idempotencyTokenService.issue(CREATE_FORM_TYPE, httpSession, sessionUser.userId()).token());
        prepareCodingTestModel(sessionUser, model, form, true);
        return "exam/coding-test";
    }

    @PostMapping("/exams")
    public String create(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @Valid CreateExamForm form,
            BindingResult bindingResult,
            HttpSession httpSession,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        String redirect = learnerRedirect(sessionUser);
        if (!redirect.isBlank()) { return redirect; }
        if (bindingResult.hasErrors()) {
            prepareCodingTestModel(sessionUser, model, form);
            return "exam/coding-test";
        }
        return createExam(sessionUser, form, httpSession, model, redirectAttributes, false);
    }

    @PostMapping("/exams/coding-test/dev")
    public String createDev(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @Valid CreateExamForm form,
            BindingResult bindingResult,
            HttpSession httpSession,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        String redirect = learnerRedirect(sessionUser);
        if (!redirect.isBlank()) { return redirect; }
        if (bindingResult.hasErrors()) {
            prepareCodingTestModel(sessionUser, model, form, true);
            return "exam/coding-test";
        }
        return createExam(sessionUser, form, httpSession, model, redirectAttributes, true);
    }

    private String createExam(
            SessionUser sessionUser,
            CreateExamForm form,
            HttpSession httpSession,
            Model model,
            RedirectAttributes redirectAttributes,
            boolean manualSelectionMode
    ) {
        try {
            idempotencyTokenService.requireAndConsume(form.getIdempotencyToken(), "", httpSession);
            ExamSessionResponse response = aiExamService.create(sessionUser, new CreateExamRequest(form.getSubjectId(), form.getLevelCode()));
            redirectAttributes.addAttribute("examId", response.examId());
            return "redirect:/exams/{examId}/problems/1";
        } catch (BusinessException exception) {
            form.setIdempotencyToken(idempotencyTokenService.issue(CREATE_FORM_TYPE, httpSession, sessionUser.userId()).token());
            prepareCodingTestModel(sessionUser, model, form, manualSelectionMode);
            model.addAttribute("errorMessage", exception.getMessage());
            return "exam/coding-test";
        }
    }

    @GetMapping("/exams/{examId}")
    public String detail(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long examId,
            Model model
    ) {
        String redirect = learnerRedirect(sessionUser);
        if (!redirect.isBlank()) { return redirect; }
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
            HttpSession httpSession,
            Model model
    ) {
        String redirect = learnerRedirect(sessionUser);
        if (!redirect.isBlank()) { return redirect; }
        ExamSessionResponse exam = aiExamService.detail(sessionUser, examId);
        if ("GRADED".equals(exam.status())) {
            return "redirect:/exams/{examId}/result";
        }
        ExamProblemStepResponse step = ExamProblemStepResponse.from(exam, problemNo);
        model.addAttribute("screen", "exam/detail");
        model.addAttribute("exam", exam);
        model.addAttribute("step", step);
        model.addAttribute("answerIdempotencyToken", idempotencyTokenService.issue(ANSWER_FORM_TYPE, httpSession, sessionUser.userId()).token());
        return "exam/detail";
    }

    @PostMapping("/exams/{examId}/answers/{aiProblemId}")
    public String saveAnswer(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long examId,
            @PathVariable Long aiProblemId,
            @Valid SaveExamAnswerForm form,
            HttpSession httpSession
    ) {
        String redirect = learnerRedirect(sessionUser);
        if (!redirect.isBlank()) { return redirect; }
        idempotencyTokenService.requireAndConsume(form.getIdempotencyToken(), "", httpSession);
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
            HttpSession httpSession,
            RedirectAttributes redirectAttributes
    ) {
        String redirect = learnerRedirect(sessionUser);
        if (!redirect.isBlank()) { return redirect; }
        redirectAttributes.addAttribute("examId", examId);
        redirectAttributes.addAttribute("problemNo", problemNo);
        if (bindingResult.hasErrors()) {
            return "redirect:/exams/{examId}/problems/{problemNo}";
        }
        idempotencyTokenService.requireAndConsume(form.getIdempotencyToken(), "", httpSession);
        ExamSessionResponse exam = aiExamService.saveAnswer(sessionUser, examId, aiProblemId, new SaveExamAnswerRequest(form.getAnswerText()));
        ExamProblemStepResponse step = ExamProblemStepResponse.from(exam, problemNo);
        if ("next".equals(form.getMove()) && step.allAnswered()) {
            redirectAttributes.addFlashAttribute("finalSubmitReady", true);
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
        String redirect = learnerRedirect(sessionUser);
        if (!redirect.isBlank()) { return redirect; }
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
        String redirect = learnerRedirect(sessionUser);
        if (!redirect.isBlank()) { return redirect; }
        aiExamService.submit(sessionUser, examId);
        return "redirect:/exams/{examId}/result";
    }

    @PostMapping("/exams/{examId}/retry-execution")
    public String retryExecution(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long examId
    ) {
        String redirect = learnerRedirect(sessionUser);
        if (!redirect.isBlank()) { return redirect; }
        aiExamService.retryExecution(sessionUser, examId);
        return "redirect:/exams/{examId}/result";
    }

    @GetMapping("/exams/{examId}/result")
    public String result(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long examId,
            Model model
    ) {
        String redirect = learnerRedirect(sessionUser);
        if (!redirect.isBlank()) { return redirect; }
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

    private void prepareCodingTestModel(SessionUser sessionUser, Model model, CreateExamForm form) {
        prepareCodingTestModel(sessionUser, model, form, false);
    }

    private void prepareCodingTestModel(SessionUser sessionUser, Model model, CreateExamForm form, boolean manualSelectionMode) {
        boolean hasGateContext = form.getSubjectId() != null && form.getLevelCode() != null && !form.getLevelCode().isBlank();
        model.addAttribute("screen", "exam/coding-test");
        model.addAttribute("form", form);
        model.addAttribute("eligibility", aiExamService.eligibility(sessionUser));
        model.addAttribute("hasGateContext", hasGateContext);
        model.addAttribute("manualSelectionMode", manualSelectionMode);
        model.addAttribute("subjectOptions", activeSubjects());
        model.addAttribute("levelOptions", LEVEL_CODES);
        model.addAttribute("selectedSubjectName", subjectDisplayName(form.getSubjectId()));
        model.addAttribute("selectedLevelCode", form.getLevelCode());
    }

    private List<Subject> activeSubjects() {
        return subjectMapper.findAll().stream()
                .filter(subject -> !Boolean.FALSE.equals(subject.getIsActive()))
                .toList();
    }

    private Long defaultSubjectId() {
        return activeSubjects().stream()
                .findFirst()
                .map(Subject::getSubjectId)
                .orElse(null);
    }

    private String defaultLevelCode(String levelCode) {
        if (levelCode != null && LEVEL_CODES.contains(levelCode)) {
            return levelCode;
        }
        return "BRONZE";
    }

    private String subjectDisplayName(Long subjectId) {
        if (subjectId == null) {
            return "-";
        }
        return subjectMapper.findById(subjectId)
                .map(this::subjectDisplayName)
                .orElse("선택 과목");
    }

    private String subjectDisplayName(Subject subject) {
        if (subject.getSubjectName() != null && !subject.getSubjectName().isBlank()) {
            return subject.getSubjectName();
        }
        if (subject.getSubjectCode() != null && !subject.getSubjectCode().isBlank()) {
            return subject.getSubjectCode();
        }
        return "선택 과목";
    }

    private String learnerRedirect(SessionUser sessionUser) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        if (!sessionUser.user()) {
            return "redirect:" + sessionUser.defaultRedirectPath();
        }
        return "";
    }
}
