package com.acorn.elearning.admin.controller;

import com.acorn.elearning.admin.service.AdminStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminStatsService service;

    @GetMapping("/admin/stats")
    public String stats(@RequestParam(defaultValue = "all") String summaryScope,
                        @RequestParam(required = false) String periodUnit,
                        @RequestParam(defaultValue = "recent3") String tableRange,
                        Model model) {


        model.addAttribute("totalUsers", service.countUsers(summaryScope));
        model.addAttribute("activeUsers", service.countActiveUsers(summaryScope));
        model.addAttribute("learningCount", service.countLearning(summaryScope));
        model.addAttribute("submissionCount", service.countSubmissions(summaryScope));
        model.addAttribute("examAttemptCount", service.countExamAttempts(summaryScope));

        model.addAttribute("selectedSummaryScope", summaryScope);
        model.addAttribute("selectedPeriodUnit", periodUnit);
        model.addAttribute("selectedTableRange", tableRange);
        model.addAttribute("statsRows", service.findStatsTableRows(null, tableRange));
        model.addAttribute("dailyLearningChart", service.dailyLearningChart(periodUnit, null));
        model.addAttribute("subjectCompleteChart", service.subjectCompleteChart(periodUnit, null, null));
        model.addAttribute("subjectExamScoreChart", service.subjectExamScoreChart(periodUnit, null));
        model.addAttribute("screen", "admin/stats");
        return "admin/stats";
    }
}
