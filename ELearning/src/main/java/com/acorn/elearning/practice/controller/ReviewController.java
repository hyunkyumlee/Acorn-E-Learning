package com.acorn.elearning.practice.controller;

import com.acorn.elearning.practice.form.WrongAnswerRetryForm;
import com.acorn.elearning.practice.service.WrongAnswerService;
import com.acorn.elearning.practice.view.WrongAnswerDetailView;
import com.acorn.elearning.practice.view.WrongAnswerPageView;
import com.acorn.elearning.practice.view.WrongAnswerSummaryView;
import com.acorn.elearning.security.SessionUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ReviewController {

    final WrongAnswerService wrongAnswerService;

    public ReviewController(WrongAnswerService wrongAnswerService) {
        this.wrongAnswerService = wrongAnswerService;
    }

    // 1. 오답 요약 페이지
    @GetMapping("/learning/review")
    public String summary(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            Model model) {

        WrongAnswerSummaryView view = wrongAnswerService.summary(sessionUser);

        model.addAttribute("view", view);
        model.addAttribute("screen", "learning/review");
        return "learning/review";
    }

    //2. 오답목록 - 현재 lessonid 기준
    @GetMapping("/learning/review/list")
    public String list(@SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
                       @RequestParam(name = "nodeId", required = false) Long nodeId,
                       @RequestParam(name = "lessonId", required = false) Long lessonId,
                       Model model) {
        WrongAnswerPageView view = wrongAnswerService.list(sessionUser, nodeId, lessonId);
        model.addAttribute("view", view);
        model.addAttribute("nodeId", nodeId);
        model.addAttribute("lessonId", lessonId);
        return "learning/review-list";
}

    @GetMapping("/learning/review/{wrongAnswerId}")
    public String detail(@PathVariable Long wrongAnswerId,
                         @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
                         Model model) {
        WrongAnswerDetailView view = wrongAnswerService.detail(sessionUser, wrongAnswerId);
        model.addAttribute("view", view);
        model.addAttribute("screen", "learning/review");
        return "learning/review";
    }

    @PostMapping("/learning/review/{wrongAnswerId}/retry")
    public String retry(@PathVariable Long wrongAnswerId,
                        @RequestParam(name = "lessonId", required = false) Long lessonId,
                        @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
                        @Validated @ModelAttribute("form") WrongAnswerRetryForm form,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            // 검증 실패 시 다시 상세 페이지로 (모델 데이터 복구 필요 시 처리)
            return "learning/review";
        }
        /*
        wrongAnswerService.retry(sessionUser, form, wrongAnswerId);
        redirectAttributes.addFlashAttribute("message", "재시도 처리가 완료되었습니다.");
        boolean correct = wrongAnswerService.retry(sessionUser, form, wrongAnswerId);
        */

        boolean correct = wrongAnswerService.retry(sessionUser, form, wrongAnswerId);
        redirectAttributes.addFlashAttribute(
        "retryResultMessage",
        correct ? "정답입니다." : "오답입니다."
        );
        if (lessonId != null) {
            redirectAttributes.addFlashAttribute("lessonId", lessonId);
        }

        return "redirect:/learning/review/" + wrongAnswerId;
    }


    @PostMapping("/learning/review/{wrongAnswerId}/reviewed")
    public String markReviewed(@PathVariable Long wrongAnswerId,
                               @RequestParam(name = "lessonId", required = false) Long lessonId,
                               @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
                               RedirectAttributes redirectAttributes) {

        wrongAnswerService.markReviewed(sessionUser, wrongAnswerId);
        redirectAttributes.addFlashAttribute("message", "검토 완료되었습니다.");

        if (lessonId != null) {
            return "redirect:/learning/review/list?lessonId=" + lessonId;
        }

        return "redirect:/learning/review";
    }

}
