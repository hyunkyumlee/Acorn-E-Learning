package com.acorn.elearning.admin.controller;

import com.acorn.elearning.admin.form.ReportHandleForm;
import com.acorn.elearning.admin.service.AdminReportService;
import com.acorn.elearning.security.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService service;


    @GetMapping("/admin/reports")
    public String reports(Model model,
                          @RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String targetType,
                          @RequestParam(required = false) String status,
                          @RequestParam(required = false) String reportDate) {

        model.addAttribute("reportPage", service.findPage(page, size, targetType, status, reportDate));
        model.addAttribute("selectedTargetType", targetType);
        model.addAttribute("selectedReportStatus", status);
        model.addAttribute("selectedReportDate", reportDate);

        model.addAttribute("screen", "admin/reports");
        return "admin/reports";
    }

    @PostMapping("/admin/reports/{reportId}")
    public String handle(
            @PathVariable Long reportId,
            ReportHandleForm form,
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            RedirectAttributes redirectAttributes
    ) {

        service.handle(reportId, form, sessionUser);
        redirectAttributes.addFlashAttribute("message", "신고 처리 상태가 변경되었습니다.");
        return "redirect:/admin/reports";
    }
}
