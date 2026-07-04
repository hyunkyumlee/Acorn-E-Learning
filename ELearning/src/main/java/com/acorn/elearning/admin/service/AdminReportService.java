package com.acorn.elearning.admin.service;

import java.time.LocalDateTime;
import java.util.List;

import com.acorn.elearning.admin.dto.response.ReportPageResponse;
import com.acorn.elearning.admin.form.ReportHandleForm;
import com.acorn.elearning.admin.mapper.AdminReportMapper;
import com.acorn.elearning.admin.model.AdminOperationLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final AdminReportMapper rm;

    private final AdminLogService adminLogService;


    public ReportPageResponse findPage() {
        List<ReportPageResponse.ReportItem> reports = rm.findAll();
        return new ReportPageResponse(reports);
    }

    public void handle(Long reportId, ReportHandleForm form) {
        int updated = rm.updateStatus(reportId, form.getStatus());

        if (updated == 1) {
            AdminOperationLog log = new AdminOperationLog();

            log.setAdminId(1L); // 임시 관리자 ID
            log.setActionType("REPORT_STATUS_UPDATE");
            log.setTargetType("REPORT");
            log.setTargetId(reportId);
            log.setResultStatus("SUCCESS");
            log.setCreatedAt(LocalDateTime.now());

            adminLogService.insert(log);
        }
    }


}
