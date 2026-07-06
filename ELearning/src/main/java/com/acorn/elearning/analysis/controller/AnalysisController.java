package com.acorn.elearning.analysis.controller;

import com.acorn.elearning.analysis.dto.response.AnalysisDashboardResponse;
import com.acorn.elearning.analysis.service.AnalysisDashboardService;
import com.acorn.elearning.analysis.service.AiAnalysisService;
import com.acorn.elearning.security.SessionUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

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
        boolean autoRefreshRequired = aiAnalysisService.latestRefreshRequired(sessionUser);
        AnalysisDashboardResponse dashboard = analysisDashboardService.dashboard(sessionUser);
        model.addAttribute("screen", "analysis/index");
        model.addAttribute("dashboard", dashboard);
        model.addAttribute("report", dashboard.report());
        model.addAttribute("autoRefreshRequired", autoRefreshRequired);
        return "analysis/index";
    }

    @GetMapping("/analysis/payment")
    public String paymentEntry() {
        return "redirect:/payments";
    }
}
