package com.acorn.elearning.community.service;

import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class PostService {
    public Map<String, Object> stub(String action) {
        // TODO 구현 예시입니다. 실제 parameter와 return DTO로 method signature를 교체하세요.
        // PostSearchCondition condition = ...;
        // List<CommunityPost> posts = communityPostMapper.findPage(condition);
        // long total = communityPostMapper.count(condition);
        // return Map.of("posts", PageResponse.of(posts, condition.page(), condition.size(), total));
        return Map.of("action", action, "status", "SKELETON");
    }
}
