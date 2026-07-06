package com.acorn.elearning.admin.service;

import java.time.LocalDateTime;

import com.acorn.elearning.admin.dto.response.AdminCommunityPageResponse;
import com.acorn.elearning.admin.form.CommunityStatusForm;
import com.acorn.elearning.admin.mapper.AdminCommunityMapper;
import com.acorn.elearning.admin.model.AdminOperationLog;
import com.acorn.elearning.security.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminCommunityService {

    private final AdminCommunityMapper cm;

    private final AdminLogService adminLogService;

    public AdminCommunityPageResponse findPage() {
        return new AdminCommunityPageResponse(cm.findPosts(), cm.findComments());
    }

    public void updatePostStatus(Long postId, CommunityStatusForm form, SessionUser sessionUser) {
        int updated = cm.updatePostStatus(postId, form.getStatus());

        if (updated == 1) {
            adminLogService.insert(operationLog(sessionUser, "COMMUNITY_POST_STATUS_UPDATE", "POST", postId));
        }
    }

    public void updateCommentStatus(Long commentId, CommunityStatusForm form, SessionUser sessionUser) {
        int updated = cm.updateCommentStatus(commentId, form.getStatus());

        if (updated == 1) {
            adminLogService.insert(operationLog(sessionUser, "COMMUNITY_COMMENT_STATUS_UPDATE", "COMMENT", commentId));
        }
    }

    private AdminOperationLog operationLog(SessionUser sessionUser, String actionType, String targetType, Long targetId) {
        AdminOperationLog log = new AdminOperationLog();
        log.setAdminId(requireAdminId(sessionUser));
        log.setActionType(actionType);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setResultStatus("SUCCESS");
        log.setCreatedAt(LocalDateTime.now());
        return log;
    }

    private Long requireAdminId(SessionUser sessionUser) {
        if (sessionUser == null || sessionUser.userId() == null) {
            throw new IllegalStateException("로그인한 관리자 정보가 없습니다.");
        }
        return sessionUser.userId();
    }
}
