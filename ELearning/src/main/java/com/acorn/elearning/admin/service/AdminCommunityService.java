package com.acorn.elearning.admin.service;

import java.time.LocalDateTime;

import com.acorn.elearning.admin.dto.response.AdminCommunityPageResponse;
import com.acorn.elearning.admin.form.CommunityStatusForm;
import com.acorn.elearning.admin.mapper.AdminCommunityMapper;
import com.acorn.elearning.admin.model.AdminOperationLog;
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

    public void updatePostStatus(Long postId, CommunityStatusForm form) {
        int updated = cm.updatePostStatus(postId, form.getStatus());

        if (updated == 1) {
            adminLogService.insert(operationLog("COMMUNITY_POST_STATUS_UPDATE", "POST", postId));
        }
    }

    public void updateCommentStatus(Long commentId, CommunityStatusForm form) {
        int updated = cm.updateCommentStatus(commentId, form.getStatus());

        if (updated == 1) {
            adminLogService.insert(operationLog("COMMUNITY_COMMENT_STATUS_UPDATE", "COMMENT", commentId));
        }
    }

    private AdminOperationLog operationLog(String actionType, String targetType, Long targetId) {
        AdminOperationLog log = new AdminOperationLog();
        log.setAdminId(1L); // 임시 관리자 ID
        log.setActionType(actionType);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setResultStatus("SUCCESS");
        log.setCreatedAt(LocalDateTime.now());
        return log;
    }
}
