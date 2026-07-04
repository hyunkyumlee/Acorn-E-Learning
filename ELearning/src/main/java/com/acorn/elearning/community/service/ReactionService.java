package com.acorn.elearning.community.service;

import com.acorn.elearning.common.exception.BusinessException;
import com.acorn.elearning.common.exception.ErrorCode;
import com.acorn.elearning.community.dto.response.ReactionResponse;
import com.acorn.elearning.community.mapper.CommunityPostMapper;
import com.acorn.elearning.community.mapper.PostLikeMapper;
import com.acorn.elearning.community.mapper.PostScrapMapper;
import com.acorn.elearning.community.model.CommunityPost;
import com.acorn.elearning.community.model.PostLike;
import com.acorn.elearning.community.model.PostScrap;
import com.acorn.elearning.security.SessionUser;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReactionService {
    private final CommunityPostMapper communityPostMapper;
    private final PostLikeMapper postLikeMapper;
    private final PostScrapMapper postScrapMapper;

    public ReactionService(
            CommunityPostMapper communityPostMapper,
            PostLikeMapper postLikeMapper,
            PostScrapMapper postScrapMapper
    ) {
        this.communityPostMapper = communityPostMapper;
        this.postLikeMapper = postLikeMapper;
        this.postScrapMapper = postScrapMapper;
    }

    public Map<String, Object> stub(String action) {
        return Map.of("action", action, "status", "IMPLEMENTED");
    }

    @Transactional
    public ReactionResponse like(SessionUser sessionUser, Long postId) {
        Long userId = requireUserId(sessionUser);
        CommunityPost post = requireActivePost(postId);
        if (postLikeMapper.findByPostIdAndUserId(postId, userId).isPresent()) {
            return new ReactionResponse(postId, "LIKE", true, safeCount(post.getLikeCount()));
        }
        PostLike like = new PostLike();
        like.setPostId(postId);
        like.setUserId(userId);
        postLikeMapper.insert(like);
        communityPostMapper.incrementLikeCount(postId);
        return new ReactionResponse(postId, "LIKE", true, safeCount(post.getLikeCount()) + 1);
    }

    @Transactional
    public ReactionResponse unlike(SessionUser sessionUser, Long postId) {
        Long userId = requireUserId(sessionUser);
        CommunityPost post = requireActivePost(postId);
        int deleted = postLikeMapper.deleteByPostIdAndUserId(postId, userId);
        if (deleted > 0) {
            communityPostMapper.decrementLikeCount(postId);
        }
        return new ReactionResponse(postId, "LIKE", false, Math.max(safeCount(post.getLikeCount()) - deleted, 0));
    }

    @Transactional
    public ReactionResponse scrap(SessionUser sessionUser, Long postId) {
        Long userId = requireUserId(sessionUser);
        CommunityPost post = requireActivePost(postId);
        if (postScrapMapper.findByPostIdAndUserId(postId, userId).isPresent()) {
            return new ReactionResponse(postId, "SCRAP", true, safeCount(post.getScrapCount()));
        }
        PostScrap scrap = new PostScrap();
        scrap.setPostId(postId);
        scrap.setUserId(userId);
        postScrapMapper.insert(scrap);
        communityPostMapper.incrementScrapCount(postId);
        return new ReactionResponse(postId, "SCRAP", true, safeCount(post.getScrapCount()) + 1);
    }

    @Transactional
    public ReactionResponse unscrap(SessionUser sessionUser, Long postId) {
        Long userId = requireUserId(sessionUser);
        CommunityPost post = requireActivePost(postId);
        int deleted = postScrapMapper.deleteByPostIdAndUserId(postId, userId);
        if (deleted > 0) {
            communityPostMapper.decrementScrapCount(postId);
        }
        return new ReactionResponse(postId, "SCRAP", false, Math.max(safeCount(post.getScrapCount()) - deleted, 0));
    }

    private CommunityPost requireActivePost(Long postId) {
        return communityPostMapper.findActiveById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND, "게시글을 찾을 수 없습니다."));
    }

    private int safeCount(Integer count) {
        return count == null ? 0 : count;
    }

    private Long requireUserId(SessionUser sessionUser) {
        if (sessionUser == null || sessionUser.userId() == null) {
            throw new BusinessException(ErrorCode.AUTH_REQUIRED);
        }
        return sessionUser.userId();
    }
}