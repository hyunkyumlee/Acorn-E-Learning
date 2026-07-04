package com.acorn.elearning.community.dto.response;

import com.acorn.elearning.community.form.PostSearchCondition;
import com.acorn.elearning.community.model.CommunityPost;
import java.util.List;

public record PostPageResponse(
        List<CommunityPost> posts,
        long total,
        int page,
        int size,
        int totalPages,
        String sort
) {
    public static PostPageResponse of(List<CommunityPost> posts, long total, PostSearchCondition condition) {
        int size = condition.normalizedSize();
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / size);
        return new PostPageResponse(posts, total, condition.normalizedPage(), size, totalPages, condition.getSort());
    }
}
