package com.acorn.elearning.community.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.community.dto.response.ReportResponse;
import com.acorn.elearning.community.form.ReportForm;
import com.acorn.elearning.community.mapper.CommentMapper;
import com.acorn.elearning.community.mapper.CommunityPostMapper;
import com.acorn.elearning.community.mapper.ReportMapper;
import com.acorn.elearning.community.model.Report;
import com.acorn.elearning.security.SessionUser;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {
    private static final String TARGET_POST = "POST";
    private static final String TARGET_COMMENT = "COMMENT";
    private static final String STATUS_RECEIVED = "RECEIVED";

    private final ReportMapper reportMapper;
    private final CommunityPostMapper communityPostMapper;
    private final CommentMapper commentMapper;

    public ReportService(
            ReportMapper reportMapper,
            CommunityPostMapper communityPostMapper,
            CommentMapper commentMapper
    ) {
        this.reportMapper = reportMapper;
        this.communityPostMapper = communityPostMapper;
        this.commentMapper = commentMapper;
    }

    public Map<String, Object> stub(String action) {
        return Map.of("action", action, "status", "IMPLEMENTED");
    }

    @Transactional
    public ReportResponse create(SessionUser sessionUser, ReportForm form) {
        Long reporterId = requireUserId(sessionUser);
        requireReportForm(form);
        String targetType = form.getTargetType().trim().toUpperCase();
        requireTarget(targetType, form.getTargetId());

        if (reportMapper.countByTargetAndReporter(targetType, form.getTargetId(), reporterId) > 0) {
            throw new BusinessException(ErrorCode.COMMON_IDEMPOTENCY_CONFLICT, "이미 신고한 대상입니다.");
        }

        Report report = new Report();
        report.setTargetType(targetType);
        report.setTargetId(form.getTargetId());
        report.setReporterId(reporterId);
        report.setReasonCode(form.getReasonCode().trim().toUpperCase());
        report.setStatus(STATUS_RECEIVED);
        reportMapper.insert(report);
        return ReportResponse.from(report);
    }

    private void requireTarget(String targetType, Long targetId) {
        if (TARGET_POST.equals(targetType)) {
            communityPostMapper.findActiveById(targetId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "신고할 게시글을 찾을 수 없습니다."));
            return;
        }
        if (TARGET_COMMENT.equals(targetType)) {
            commentMapper.findById(targetId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "신고할 댓글을 찾을 수 없습니다."));
            return;
        }
        throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "신고 대상은 POST 또는 COMMENT만 가능합니다.");
    }

    private void requireReportForm(ReportForm form) {
        if (form == null || form.getTargetId() == null
                || form.getTargetType() == null || form.getTargetType().isBlank()
                || form.getReasonCode() == null || form.getReasonCode().isBlank()) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED);
        }
    }

    private Long requireUserId(SessionUser sessionUser) {
        if (sessionUser == null || sessionUser.userId() == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
        return sessionUser.userId();
    }
}