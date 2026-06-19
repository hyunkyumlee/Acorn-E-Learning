package com.acorn.elearning.community.service;

import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class CommentService {
    public Map<String, Object> stub(String action) {
        // TODO 구현 예시입니다. 실제 parameter와 return DTO로 method signature를 교체하세요.
        // CommunityPost post = communityPostMapper.findById(postId).orElseThrow(() -> new BusinessException(ErrorCode.COMMON_NOT_FOUND));
        // Comment comment = Comment.create(post.id(), sessionUser.userId(), form.getContent());
        // commentMapper.insert(comment);
        // return Map.of("comment", CommentResponse.from(comment));
        return Map.of("action", action, "status", "SKELETON");
    }
}
