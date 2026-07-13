package com.acorn.elearning.admin.service;

import java.time.LocalDateTime;
import java.util.List;

import com.acorn.elearning.admin.dto.response.AdminPageResponse;
import com.acorn.elearning.admin.dto.response.ReportPageResponse;
import com.acorn.elearning.admin.form.ReportHandleForm;
import com.acorn.elearning.admin.mapper.AdminReportMapper;
import com.acorn.elearning.admin.model.AdminOperationLog;
import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.security.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final AdminReportMapper rm;
    private final AdminLogService adminLogService;

    public AdminPageResponse<ReportPageResponse.ReportItem> findPage(
            int page, int size, String targetType, String status, String reportDate
    ) {
        int currentPage = Math.max(page, 1);
        int pageSize = Math.max(size, 1);
        int offset = (currentPage - 1) * pageSize;

        List<ReportPageResponse.ReportItem> items = rm.findPage(
                pageSize, offset, targetType, status, reportDate
        );
        long totalCount = rm.countAll(targetType, status, reportDate);

        return new AdminPageResponse<>(items, currentPage, pageSize, totalCount);
    }

    @Transactional
    public int handle(Long reportId, ReportHandleForm form, SessionUser sessionUser) {
        ReportPageResponse.ReportItem report = rm.findById(reportId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.COMMON_NOT_FOUND,
                        "처리할 신고를 찾을 수 없습니다."
                ));

        int updated = rm.updateStatus(reportId, form.getStatus());
        if (updated == 1) {
            adminLogService.insert(operationLog(
                    sessionUser,
                    reportId,
                    report.targetSummary(),
                    reportChangeDetail(form.getStatus())
            ));
        }

        return updated;
    }

    private AdminOperationLog operationLog(
            SessionUser sessionUser, Long reportId, String targetName, String changeDetail
    ) {
        AdminOperationLog log = new AdminOperationLog();
        log.setAdminId(requireAdminId(sessionUser));
        log.setActionType("REPORT_STATUS_UPDATE");
        log.setTargetType("REPORT");
        log.setTargetId(reportId);
        log.setTargetName(targetName == null || targetName.isBlank() ? "신고 ID " + reportId : targetName);
        log.setChangeDetail(changeDetail);
        log.setResultStatus("SUCCESS");
        log.setCreatedAt(LocalDateTime.now());
        return log;
    }

    private String reportChangeDetail(String status) {
        return switch (status) {
            case "IN_PROGRESS" -> "신고를 검토 중으로 변경";
            case "RESOLVED" -> "신고 처리를 완료";
            case "REJECTED" -> "신고를 반려";
            default -> "신고 상태를 " + status + "로 변경";
        };
    }

    private Long requireAdminId(SessionUser sessionUser) {
        if (sessionUser == null || sessionUser.userId() == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED, "로그인한 관리자 정보가 없습니다.");
        }
        return sessionUser.userId();
    }
}
