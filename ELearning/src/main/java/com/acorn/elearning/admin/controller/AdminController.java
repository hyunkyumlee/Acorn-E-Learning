package com.acorn.elearning.admin.controller;

import com.acorn.elearning.admin.service.AdminStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final AdminStatsService statsService;
    @GetMapping("/admin")
    public String dashboard(Model model) {

        model.addAttribute("totalUserCount", statsService.countTotalUsers());
        model.addAttribute("todayLearningCount", statsService.countTodayLearning());
        model.addAttribute("todaySubmissionCount", statsService.countTodaySubmissions());
        model.addAttribute("pendingReportCount", statsService.countPendingReports());

        model.addAttribute("recentReports", statsService.findRecentReports());
        model.addAttribute("recentNotices", statsService.findRecentNotices());

        model.addAttribute("dailyLearningChart", statsService.dailyLearningChart());
        model.addAttribute("subjectCompleteChart", statsService.subjectCompleteChart());
        model.addAttribute("screen", "admin/dashboard");
        return "admin/dashboard";
    }
}
