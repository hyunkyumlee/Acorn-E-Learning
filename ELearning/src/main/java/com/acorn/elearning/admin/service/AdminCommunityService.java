package com.acorn.elearning.admin.service;

import java.time.LocalDateTime;
import java.util.List;

import com.acorn.elearning.admin.dto.response.AdminCommunityPageResponse;
import com.acorn.elearning.admin.dto.response.AdminPageResponse;
import com.acorn.elearning.admin.form.CommunityStatusForm;
import com.acorn.elearning.admin.mapper.AdminCommunityMapper;
import com.acorn.elearning.admin.model.AdminOperationLog;
import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.security.SessionUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminCommunityService {

    private final AdminCommunityMapper cm;
    private final AdminLogService adminLogService;

    public AdminCommunityPageResponse findPage(
            int postPage,
            int commentPage,
            int size,
            String boardType,
            String status,
            String keyword
    ) {
        return findPage(postPage, commentPage, size, boardType, status, keyword, status, keyword);
    }

    public AdminCommunityPageResponse findPage(
            int postPage,
            int commentPage,
            int size,
            String postBoardType,
            String postStatus,
            String postKeyword,
            String commentStatus,
            String commentKeyword
    ) {
        int pageSize = Math.max(size, 1);
        int currentPostPage = Math.max(postPage, 1);
        int currentCommentPage = Math.max(commentPage, 1);

        List<AdminCommunityPageResponse.PostItem> posts = cm.findPostPage(
                pageSize, (currentPostPage - 1) * pageSize, postBoardType, postStatus, postKeyword
        );
        long postTotalCount = cm.countPosts(postBoardType, postStatus, postKeyword);

        List<AdminCommunityPageResponse.CommentItem> comments = cm.findCommentPage(
                pageSize, (currentCommentPage - 1) * pageSize, commentStatus, commentKeyword
        );
        long commentTotalCount = cm.countComments(commentStatus, commentKeyword);

        return new AdminCommunityPageResponse(
                new AdminPageResponse<>(posts, currentPostPage, pageSize, postTotalCount),
                new AdminPageResponse<>(comments, currentCommentPage, pageSize, commentTotalCount)
        );
    }

    @Transactional
    public int updatePostStatus(Long postId, CommunityStatusForm form, SessionUser sessionUser) {
        AdminCommunityPageResponse.PostItem post = cm.findPostById(postId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.COMMON_NOT_FOUND,
                        "상태를 변경할 게시글을 찾을 수 없습니다."
                ));

        int updated = cm.updatePostStatus(postId, form.getStatus());
        if (updated == 1) {
            adminLogService.insert(operationLog(
                    sessionUser,
                    "COMMUNITY_POST_STATUS_UPDATE",
                    "POST",
                    postId,
                    post.title(),
                    statusChangeDetail("게시글", form.getStatus())
            ));
        }

        return updated;
    }

    @Transactional
    public int updateCommentStatus(Long commentId, CommunityStatusForm form, SessionUser sessionUser) {
        AdminCommunityPageResponse.CommentItem comment = cm.findCommentById(commentId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.COMMON_NOT_FOUND,
                        "상태를 변경할 댓글을 찾을 수 없습니다."
                ));

        int updated = cm.updateCommentStatus(commentId, form.getStatus());
        if (updated == 1) {
            adminLogService.insert(operationLog(
                    sessionUser,
                    "COMMUNITY_COMMENT_STATUS_UPDATE",
                    "COMMENT",
                    commentId,
                    comment.contentSummary(),
                    statusChangeDetail("댓글", form.getStatus())
            ));
        }

        return updated;
    }

    private AdminOperationLog operationLog(
            SessionUser sessionUser,
            String actionType,
            String targetType,
            Long targetId,
            String targetName,
            String changeDetail
    ) {
        AdminOperationLog log = new AdminOperationLog();
        log.setAdminId(requireAdminId(sessionUser));
        log.setActionType(actionType);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setTargetName(targetName);
        log.setChangeDetail(changeDetail);
        log.setResultStatus("SUCCESS");
        log.setCreatedAt(LocalDateTime.now());
        return log;
    }

    private String statusChangeDetail(String targetLabel, String status) {
        return switch (status) {
            case "ACTIVE" -> targetLabel + "을 공개 처리";
            case "HIDDEN" -> targetLabel + "을 숨김 처리";
            case "DELETED" -> targetLabel + "을 삭제 처리";
            default -> targetLabel + " 상태를 " + status + "로 변경";
        };
    }

    private Long requireAdminId(SessionUser sessionUser) {
        if (sessionUser == null || sessionUser.userId() == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED, "로그인한 관리자 정보가 없습니다.");
        }
        return sessionUser.userId();
    }
}
