package com.acorn.elearning.analysis.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AnalysisController {

    @GetMapping("/analysis")
    public String index(Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // AnalysisDashboardView view = analysisService.index(sessionUser);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.
        model.addAttribute("screen", "analysis/index");
        return "analysis/index";
    }

    @PostMapping("/analysis")
    public String generate() {
        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
        // if (bindingResult.hasErrors()) { return "analysis/index"; }
        // SessionUser sessionUser = currentSessionUser();
        // aiAnalysisService.generate(sessionUser, form);
        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
        return "redirect:/analysis";
    }

    @PostMapping("/analysis/{reportId}/retry")
    public String retry(@PathVariable Long reportId) {
        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
        // if (bindingResult.hasErrors()) { return "/analysis"; }
        // SessionUser sessionUser = currentSessionUser();
        // aiAnalysisService.retry(sessionUser, form, reportId);
        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
        return "redirect:/analysis";
    }

    @GetMapping("/analysis/payment")
    public String paymentEntry(Model model) {
        // TODO 구현 예시입니다. 이 method는 화면 렌더링이 아니라 browser redirect flow입니다.
        // SessionUser sessionUser = currentSessionUser();
        // RedirectTarget redirectTarget = paymentAccessService.requirePremiumOrPaymentRedirect(sessionUser);
        // return "redirect:" + redirectTarget.url();

        model.addAttribute("screen", "redirect:/payments");

        return "redirect:/payments";
    }
}
