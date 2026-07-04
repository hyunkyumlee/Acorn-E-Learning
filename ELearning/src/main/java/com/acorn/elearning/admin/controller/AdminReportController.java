package com.acorn.elearning.admin.controller;

import com.acorn.elearning.admin.form.ReportHandleForm;
import com.acorn.elearning.admin.service.AdminReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService service;


    @GetMapping("/admin/reports")
    public String reports(Model model) {
        // TODO 구현 예시입니다. 실제 signature에 HttpSession 또는 SessionUser를 추가하세요.
        // SessionUser sessionUser = currentSessionUser();
        // AdminManagePageView view = adminReportService.reports(sessionUser);
        // model.addAttribute("view", view);
        // 필요한 경우 model.addAttribute("form", new XxxForm()); 값도 같이 넣으세요.

        model.addAttribute("reportPage", service.findPage());

        model.addAttribute("screen", "admin/reports");
        return "admin/reports";
    }

    @PostMapping("/admin/reports/{reportId}")
    public String handle(@PathVariable Long reportId, ReportHandleForm form) {
        // TODO 구현 예시입니다. 실제 signature에 @Validated Form, BindingResult, RedirectAttributes를 추가하세요.
        // if (bindingResult.hasErrors()) { return "admin/reports"; }
        // SessionUser sessionUser = currentSessionUser();
        // adminCommunityService.handle(sessionUser, form);
        // redirectAttributes.addFlashAttribute("message", "처리되었습니다.");
        service.handle(reportId, form);
        return "redirect:/admin/reports";
    }
}
