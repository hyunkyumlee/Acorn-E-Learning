package com.acorn.elearning.exam.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ExamController {

    @GetMapping("/exams/coding-test")
    public String codingTest(Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // ExamSessionView view = examService.codingTest(sessionUser);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
        model.addAttribute("screen", "exam/coding-test");
        return "exam/coding-test";
    }

    @PostMapping("/exams")
    public String create() {
        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
        // if (bindingResult.hasErrors()) { return "exam/coding-test"; }
        // SessionUser sessionUser = currentSessionUser();
        // aiExamService.create(sessionUser, form);
        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
        return "redirect:/exams/coding-test";
    }

    @PostMapping("/exams/{examId}/answers/{aiProblemId}")
    public String saveAnswer(@PathVariable Long examId, @PathVariable Long aiProblemId) {
        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
        // if (bindingResult.hasErrors()) { return "/exams/coding-test"; }
        // SessionUser sessionUser = currentSessionUser();
        // aiGradingService.saveAnswer(sessionUser, form, examId, aiProblemId);
        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
        return "redirect:/exams/coding-test";
    }

    @PostMapping("/exams/{examId}/submit")
    public String submit(@PathVariable Long examId) {
        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
        // if (bindingResult.hasErrors()) { return "/exams/{examId}/result"; }
        // SessionUser sessionUser = currentSessionUser();
        // aiGradingService.submit(sessionUser, form, examId);
        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
        return "redirect:/exams/{examId}/result";
    }

    @PostMapping("/exams/{examId}/retry-grading")
    public String retryGrading(@PathVariable Long examId) {
        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
        // if (bindingResult.hasErrors()) { return "/exams/{examId}/result"; }
        // SessionUser sessionUser = currentSessionUser();
        // aiGradingService.retryGrading(sessionUser, form, examId);
        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
        return "redirect:/exams/{examId}/result";
    }

    @GetMapping("/exams/{examId}/result")
    public String result(@PathVariable Long examId, Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // ExamResultView view = examService.result(sessionUser, examId);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
        model.addAttribute("screen", "exam/result");
        return "exam/result";
    }
}
