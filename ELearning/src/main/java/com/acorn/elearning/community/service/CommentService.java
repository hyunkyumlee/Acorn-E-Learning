package com.acorn.elearning.community.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.community.dto.response.CommentListResponse;
import com.acorn.elearning.community.dto.response.CommentResponse;
import com.acorn.elearning.community.form.CommentForm;
import com.acorn.elearning.community.mapper.CommentMapper;
import com.acorn.elearning.community.mapper.CommunityPostMapper;
import com.acorn.elearning.community.model.Comment;
import com.acorn.elearning.community.model.CommunityPost;
import com.acorn.elearning.security.SessionUser;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_HIDDEN = "HIDDEN";
    private static final String STATUS_DELETED = "DELETED";

    private final CommentMapper commentMapper;
    private final CommunityPostMapper communityPostMapper;

    public CommentService(CommentMapper commentMapper, CommunityPostMapper communityPostMapper) {
        this.commentMapper = commentMapper;
        this.communityPostMapper = communityPostMapper;
    }

    public Map<String, Object> stub(String action) {
        return Map.of("action", action, "status", "IMPLEMENTED");
    }

    @Transactional(readOnly = true)
    public CommentListResponse comments(Long postId) {
        requireActivePost(postId);
        return new CommentListResponse(commentMapper.findByPostId(postId).stream()
                .map(this::maskIfDeleted)
                .toList());
    }

    @Transactional
    public CommentResponse create(SessionUser sessionUser, Long postId, CommentForm form) {
        Long userId = requireUserId(sessionUser);
        requireActivePost(postId);
        requireCommentForm(form);
        validateParent(postId, form.getParentCommentId());

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setParentCommentId(form.getParentCommentId());
        comment.setWriterId(userId);
        comment.setContent(form.getContent().trim());
        comment.setStatus(STATUS_ACTIVE);
        commentMapper.insert(comment);
        communityPostMapper.incrementCommentCount(postId);
        return new CommentResponse(comment);
    }

    @Transactional
    public CommentResponse update(SessionUser sessionUser, Long commentId, CommentForm form) {
        Long userId = requireUserId(sessionUser);
        requireCommentForm(form);
        Comment comment = requireComment(commentId);
        requireOwner(comment, userId);

        if(STATUS_HIDDEN.equals(comment.getStatus())){
            throw new BusinessException(
                    ErrorCode.COMMON_NOT_FOUND, "관리자에 의해 숨김 처리된 댓글은 수정할 수 없습니다."
            );
        }

        comment.setContent(form.getContent().trim());
        int updated = commentMapper.update(comment);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.COMMON_NOT_FOUND, "수정할 댓글을 찾을 수 없습니다.");
        }
        return new CommentResponse(requireComment(commentId));
    }

    @Transactional
    public void delete(SessionUser sessionUser, Long commentId) {
        Long userId = requireUserId(sessionUser);
        Comment comment = requireComment(commentId);
        requireOwner(comment, userId);

        if(STATUS_HIDDEN.equals(comment.getStatus())){
            throw new BusinessException(
                    ErrorCode.COMMON_NOT_FOUND, "관리자에 의해 숨김 처리된 댓글은 삭제할 수 없습니다."
            );
        }

        int updated = commentMapper.softDelete(commentId, userId);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.COMMON_NOT_FOUND, "삭제할 댓글을 찾을 수 없습니다.");
        }
        communityPostMapper.decrementCommentCount(comment.getPostId());
    }

    private void validateParent(Long postId, Long parentCommentId) {
        if (parentCommentId == null) {
            return;
        }
        Comment parent = requireComment(parentCommentId);
        if (!postId.equals(parent.getPostId()) || parent.getParentCommentId() != null || !STATUS_ACTIVE.equals(parent.getStatus())) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "대댓글은 같은 게시글의 댓글에만 작성할 수 있습니다.");
        }
    }

    private Comment maskIfDeleted(Comment comment) {
        if (STATUS_DELETED.equals(comment.getStatus())) {
            if (comment.getDeletedByAdminId() != null) {
                comment.setContent("관리자에 의해 삭제된 댓글입니다.");
            } else {
                comment.setContent("삭제된 댓글입니다.");
            }
        }

        if (STATUS_HIDDEN.equals(comment.getStatus())){
            comment.setContent("관리자에 의해 숨겨진 댓글입니다.");
        }
        return comment;
    }

    private CommunityPost requireActivePost(Long postId) {
        return communityPostMapper.findActiveById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "게시글을 찾을 수 없습니다."));
    }

    private Comment requireComment(Long commentId) {
        return commentMapper.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "댓글을 찾을 수 없습니다."));
    }

    private void requireCommentForm(CommentForm form) {
        if (form == null || form.getContent() == null || form.getContent().isBlank()) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED);
        }
    }

    private void requireOwner(Comment comment, Long userId) {
        if (!userId.equals(comment.getWriterId())) {
            throw new BusinessException(ErrorCode.AUTH_FORBIDDEN);
        }
    }

    private Long requireUserId(SessionUser sessionUser) {
        if (sessionUser == null || sessionUser.userId() == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
        return sessionUser.userId();
    }
}