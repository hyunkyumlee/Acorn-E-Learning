package com.acorn.elearning.community.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.community.dto.response.CommunityProfileResponse;
import com.acorn.elearning.community.dto.response.PostDetailResponse;
import com.acorn.elearning.community.dto.response.PostPageResponse;
import com.acorn.elearning.community.form.PostForm;
import com.acorn.elearning.community.form.PostSearchCondition;
import com.acorn.elearning.community.mapper.CommentMapper;
import com.acorn.elearning.community.mapper.CommunityPostMapper;
import com.acorn.elearning.community.mapper.PostAttachmentMapper;
import com.acorn.elearning.community.mapper.PostLikeMapper;
import com.acorn.elearning.community.mapper.PostScrapMapper;
import com.acorn.elearning.community.model.Comment;
import com.acorn.elearning.community.model.CommunityPost;
import com.acorn.elearning.security.SessionUser;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_HIDDEN = "HIDDEN";
    private static final String STATUS_DELETED = "DELETED";
    private static final String WEEKLY_POPULAR_LABEL = "주간 인기";
    private static final String MONTHLY_POPULAR_LABEL = "월간 인기";
    private static final Set<String> ALLOWED_BOARD_TYPES = Set.of("FREE", "QUESTION", "STUDY_LOG");



    private final CommunityPostMapper communityPostMapper;
    private final PostAttachmentMapper postAttachmentMapper;
    private final CommentMapper commentMapper;
    private final PostLikeMapper postLikeMapper;
    private final PostScrapMapper postScrapMapper;
    private final AttachmentService attachmentService;

    public PostService(
            CommunityPostMapper communityPostMapper,
            PostAttachmentMapper postAttachmentMapper,
            CommentMapper commentMapper,
            PostLikeMapper postLikeMapper,
            PostScrapMapper postScrapMapper,
            AttachmentService attachmentService
    ) {
        this.communityPostMapper = communityPostMapper;
        this.postAttachmentMapper = postAttachmentMapper;
        this.commentMapper = commentMapper;
        this.postLikeMapper = postLikeMapper;
        this.postScrapMapper = postScrapMapper;
        this.attachmentService = attachmentService;
    }

    public Map<String, Object> stub(String action) {
        return Map.of("action", action, "status", "IMPLEMENTED");
    }

    @Transactional(readOnly = true)
    public PostPageResponse page(PostSearchCondition condition) {
        PostSearchCondition searchCondition = condition == null ? new PostSearchCondition() : condition;
        List<CommunityPost> posts = communityPostMapper.findPage(searchCondition);
        applyPopularity(posts);
        return PostPageResponse.of(
                posts,
                communityPostMapper.countPage(searchCondition),
                searchCondition
        );
    }

    @Transactional
    public PostDetailResponse detail(SessionUser sessionUser, Long postId) {
        return detail(sessionUser, postId, true);
    }

    @Transactional
    public PostDetailResponse detail(SessionUser sessionUser, Long postId, boolean incrementView) {
        CommunityPost post = requireViewablePost(postId);
        if (incrementView) {
            communityPostMapper.incrementViewCount(postId);
            post.setViewCount(safeCount(post.getViewCount()) + 1);
        }
        applyPopularity(List.of(post));
        Long userId = sessionUser == null ? null : sessionUser.userId();
        boolean liked = userId != null && postLikeMapper.findByPostIdAndUserId(postId, userId).isPresent();
        boolean scraped = userId != null && postScrapMapper.findByPostIdAndUserId(postId, userId).isPresent();
        boolean owner = userId != null && userId.equals(post.getWriterId());
        return new PostDetailResponse(
                post,
                postAttachmentMapper.findByPostId(postId),
                visibleComments(postId),
                liked,
                scraped,
                owner
        );
    }

    @Transactional
    public CommunityPost create(SessionUser sessionUser, PostForm form) {
        Long userId = requireUserId(sessionUser);
        requirePostForm(form);
        String boardType = normalizeBoardType(form.getBoardType());

        CommunityPost post = new CommunityPost();
        post.setWriterId(userId);
        post.setSubjectId(form.getSubjectId());
        post.setBoardType(boardType);
        post.setTitle(form.getTitle().trim());
        post.setContent(form.getContent().trim());
        post.setStatus(STATUS_ACTIVE);
        communityPostMapper.insert(post);
        attachmentService.saveMetadata(post.getPostId(), userId, form.getFiles());
        return requireActivePost(post.getPostId());
    }

    @Transactional
    public CommunityPost createDraft(SessionUser sessionUser) {
        Long userId = requireUserId(sessionUser);
        CommunityPost draft = new CommunityPost();
        draft.setWriterId(userId);
        draft.setSubjectId(1L);
        draft.setBoardType("FREE");
        draft.setTitle("임시 작성 글");
        draft.setContent("");
        draft.setStatus(STATUS_DRAFT);
        communityPostMapper.insert(draft);
        return draft;
    }

    @Transactional
    public CommunityPost publishDraft(SessionUser sessionUser, PostForm form) {
        Long userId = requireUserId(sessionUser);
        requirePostForm(form);
        if (form.getDraftPostId() == null) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "임시 작성 글 정보가 필요합니다.");
        }

        CommunityPost draft = requireOwnedDraft(form.getDraftPostId(), userId);
        draft.setSubjectId(form.getSubjectId());
        draft.setBoardType(normalizeBoardType(form.getBoardType()));
        draft.setTitle(form.getTitle().trim());
        draft.setContent(form.getContent().trim());
        if (communityPostMapper.publishDraft(draft) == 0) {
            throw new BusinessException(ErrorCode.COMMON_NOT_FOUND, "발행할 임시 작성 글을 찾을 수 없습니다.");
        }
        attachmentService.removeUnreferencedInlineImages(draft.getPostId(), draft.getContent());
        return requireActivePost(draft.getPostId());
    }

    @Transactional
    public CommunityPost update(SessionUser sessionUser, Long postId, PostForm form) {
        Long userId = requireUserId(sessionUser);
        requirePostForm(form);
        CommunityPost post = requireActivePost(postId);
        requireOwner(post, userId);

        post.setSubjectId(form.getSubjectId());
        post.setBoardType(normalizeBoardType(form.getBoardType()));
        post.setTitle(form.getTitle().trim());
        post.setContent(form.getContent().trim());
        communityPostMapper.update(post);
        deleteSelectedAttachments(sessionUser, postId, form.getDeleteAttachmentIds());
        attachmentService.saveMetadata(postId, userId, form.getFiles());
        attachmentService.removeUnreferencedInlineImages(postId, post.getContent());
        return requireActivePost(postId);
    }

    @Transactional
    public void delete(SessionUser sessionUser, Long postId) {
        Long userId = requireUserId(sessionUser);
        CommunityPost post = requireActivePost(postId);
        requireOwner(post, userId);
        int updated = communityPostMapper.softDelete(postId, userId);
        if (updated == 0) {
            throw new BusinessException(ErrorCode.COMMON_NOT_FOUND, "삭제할 게시글을 찾을 수 없습니다.");
        }
    }

    @Transactional(readOnly = true)
    public CommunityProfileResponse profile(SessionUser sessionUser) {
        Long userId = requireUserId(sessionUser);
        return new CommunityProfileResponse(
                communityPostMapper.findByWriterId(userId),
                commentMapper.findByWriterId(userId).stream()
                        .map(this::maskIfDeleted)
                        .toList(),
                postLikeMapper.findPostsByUserId(userId),
                postScrapMapper.findPostsByUserId(userId)
        );
    }

    private CommunityPost requireActivePost(Long postId) {
        return communityPostMapper.findActiveById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "게시글을 찾을 수 없습니다."));
    }

    private CommunityPost requireViewablePost(Long postId){
        CommunityPost post = communityPostMapper.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "게시글을 찾을 수 없습니다."));

        if(STATUS_HIDDEN.equals(post.getStatus())){
            throw new BusinessException(
                    ErrorCode.COMMON_NOT_FOUND, "관리자에 의해 숨겨진 게시글입니다."
            );
        }
        
        if(STATUS_DELETED.equals(post.getStatus()) || post.getDeletedAt() != null){
            throw new BusinessException(
                    ErrorCode.COMMON_NOT_FOUND, "관리자에 의해 삭제된 게시글입니다."
            );
        }

        if (!STATUS_ACTIVE.equals(post.getStatus())) {
            throw new BusinessException(ErrorCode.COMMON_NOT_FOUND, "게시글을 찾을 수 없습니다.");
        }

        return post;
    }

    private CommunityPost requireOwnedDraft(Long postId, Long userId) {
        CommunityPost post = communityPostMapper.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "임시 작성 글을 찾을 수 없습니다."));
        requireOwner(post, userId);
        if (!STATUS_DRAFT.equals(post.getStatus()) || post.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.COMMON_NOT_FOUND, "임시 작성 글을 찾을 수 없습니다.");
        }
        return post;
    }

    private void applyPopularity(List<CommunityPost> posts) {
        Set<Long> weeklyPopularPostIds = new HashSet<>(communityPostMapper.findWeeklyPopularPostIds());
        Set<Long> monthlyPopularPostIds = new HashSet<>(communityPostMapper.findMonthlyPopularPostIds());
        for (CommunityPost post : posts) {
            if (weeklyPopularPostIds.contains(post.getPostId())) {
                post.setPopular(true);
                post.setPopularLabel(WEEKLY_POPULAR_LABEL);
            } else if (monthlyPopularPostIds.contains(post.getPostId())) {
                post.setPopular(true);
                post.setPopularLabel(MONTHLY_POPULAR_LABEL);
            } else {
                post.setPopular(false);
                post.setPopularLabel(null);
            }
        }
    }

    private List<Comment> visibleComments(Long postId) {
        return commentMapper.findByPostId(postId).stream()
                .map(this::maskIfDeleted)
                .toList();
    }

    private void deleteSelectedAttachments(SessionUser sessionUser, Long postId, List<Long> attachmentIds) {
        if (attachmentIds == null || attachmentIds.isEmpty()) {
            return;
        }
        attachmentIds.stream()
                .filter(attachmentId -> attachmentId != null)
                .distinct()
                .forEach(attachmentId ->
                        attachmentService.deleteForPost(
                                sessionUser,
                                postId,
                                attachmentId
                        )
                );
    }

    private Comment maskIfDeleted(Comment comment) {
        if(STATUS_DELETED.equals(comment.getStatus())) {
            if (comment.getDeletedByAdminId() != null) {
                comment.setContent("관리자에 의해 삭제된 댓글입니다.");
            } else {
                comment.setContent("삭제된 댓글입니다.");
            }
        }
        if(STATUS_HIDDEN.equals(comment.getStatus())){
            comment.setContent("관리자에 의해 숨김 처리된 댓글입니다.");
        }
        return comment;
    }

    private void requirePostForm(PostForm form) {
        if (form == null || blank(form.getTitle()) || blank(form.getContent())
                || form.getSubjectId() == null || blank(form.getBoardType())) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED);
        }
        normalizeBoardType(form.getBoardType());
    }

    private String normalizeBoardType(String boardType) {
        String normalized = boardType == null ? "" : boardType.trim().toUpperCase();
        if (!ALLOWED_BOARD_TYPES.contains(normalized)) {
            throw new BusinessException(ErrorCode.COMMON_VALIDATION_FAILED, "게시판은 자유, 질문, 공부 일지만 선택할 수 있습니다.");
        }
        return normalized;
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private int safeCount(Integer count) {
        return count == null ? 0 : count;
    }

    private void requireOwner(CommunityPost post, Long userId) {
        if (!userId.equals(post.getWriterId())) {
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
