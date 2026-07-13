package com.acorn.elearning.admin.controller;

import com.acorn.elearning.admin.dto.response.AdminOperationLogPageResponse;
import com.acorn.elearning.admin.dto.response.AdminPageResponse;
import com.acorn.elearning.admin.service.AdminOpsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AdminOperationLogController {

    private final AdminOpsService service;

    @GetMapping("/admin/operation-logs")
    public String operationLogs(
            Model model,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String actionCategory
    )
    {
        AdminPageResponse<AdminOperationLogPageResponse> operationLogPage =
                service.findOperationLogPage(page, 10, targetType, actionCategory);

        model.addAttribute("operationLogPage", operationLogPage);
        model.addAttribute("selectedTargetType", targetType);
        model.addAttribute("selectedActionCategory", actionCategory);

        model.addAttribute("screen", "admin/operation-logs");
        return "admin/adminLog";
    }
}
