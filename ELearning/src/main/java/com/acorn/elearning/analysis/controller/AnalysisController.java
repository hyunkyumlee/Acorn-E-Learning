package com.acorn.elearning.analysis.controller;

import com.acorn.elearning.analysis.dto.request.GenerateAnalysisRequest;
import com.acorn.elearning.analysis.dto.response.AnalysisDashboardResponse;
import com.acorn.elearning.analysis.dto.response.AnalysisReportResponse;
import com.acorn.elearning.analysis.form.GenerateAnalysisForm;
import com.acorn.elearning.analysis.service.AnalysisDashboardService;
import com.acorn.elearning.analysis.service.AiAnalysisService;
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
public class AnalysisController {
    private final AiAnalysisService aiAnalysisService;
    private final AnalysisDashboardService analysisDashboardService;

    public AnalysisController(AiAnalysisService aiAnalysisService, AnalysisDashboardService analysisDashboardService) {
        this.aiAnalysisService = aiAnalysisService;
        this.analysisDashboardService = analysisDashboardService;
    }

    @GetMapping("/analysis")
    public String index(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            Model model
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        AnalysisDashboardResponse dashboard = analysisDashboardService.dashboard(sessionUser);
        model.addAttribute("screen", "analysis/index");
        model.addAttribute("form", new GenerateAnalysisForm());
        model.addAttribute("dashboard", dashboard);
        model.addAttribute("report", dashboard.report());
        return "analysis/index";
    }

    @PostMapping("/analysis")
    public String generate(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @Valid GenerateAnalysisForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        if (bindingResult.hasErrors()) {
            AnalysisDashboardResponse dashboard = analysisDashboardService.dashboard(sessionUser);
            model.addAttribute("screen", "analysis/index");
            model.addAttribute("dashboard", dashboard);
            model.addAttribute("report", dashboard.report());
            return "analysis/index";
        }
        AnalysisReportResponse report = aiAnalysisService.generate(sessionUser, new GenerateAnalysisRequest(form.getExamId()));
        redirectAttributes.addAttribute("reportId", report.reportId());
        return "redirect:/analysis?reportId={reportId}";
    }

    @PostMapping("/analysis/{reportId}/retry")
    public String retry(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @PathVariable Long reportId
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        aiAnalysisService.retry(sessionUser, reportId);
        return "redirect:/analysis";
    }

    @GetMapping("/analysis/payment")
    public String paymentEntry() {
        return "redirect:/payments";
    }
}
