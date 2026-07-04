package com.acorn.elearning.community.controller;

import com.acorn.elearning.community.form.ReportForm;
import com.acorn.elearning.community.service.ReportService;
import com.acorn.elearning.security.SessionUser;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/community/reports")
    public String create(
            @SessionAttribute(name = SessionUser.SESSION_KEY, required = false) SessionUser sessionUser,
            @Valid @ModelAttribute("reportForm") ReportForm form,
            BindingResult bindingResult,
            @RequestParam(name = "redirectPostId", required = false) Long redirectPostId,
            RedirectAttributes redirectAttributes
    ) {
        if (sessionUser == null) {
            return "redirect:/login";
        }
        if (!bindingResult.hasErrors()) {
            reportService.create(sessionUser, form);
            redirectAttributes.addFlashAttribute("message", "신고가 접수되었습니다.");
        }
        if (redirectPostId != null) {
            return "redirect:/community/posts/" + redirectPostId;
        }
        if (form != null && "POST".equalsIgnoreCase(form.getTargetType())) {
            return "redirect:/community/posts/" + form.getTargetId();
        }
        return "redirect:/community/board";
    }
}