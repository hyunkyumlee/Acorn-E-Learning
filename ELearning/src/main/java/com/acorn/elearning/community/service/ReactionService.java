package com.acorn.elearning.community.service;

import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ReactionService {
    public Map<String, Object> stub(String action) {
        // TODO 구현 예시입니다. 실제 parameter와 return DTO로 method signature를 교체하세요.
        // CommunityPost post = communityPostMapper.findById(postId).orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND));
        // boolean active = postLikeMapper.toggle(sessionUser.userId(), post.id());
        // return Map.of("postId", post.id(), "active", active);
        return Map.of("action", action, "status", "SKELETON");
    }
}
