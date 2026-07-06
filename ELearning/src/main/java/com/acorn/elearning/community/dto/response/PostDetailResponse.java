package com.acorn.elearning.community.dto.response;

import com.acorn.elearning.community.model.Comment;
import com.acorn.elearning.community.model.CommunityPost;
import com.acorn.elearning.community.model.PostAttachment;
import java.util.List;

public record PostDetailResponse(
        CommunityPost post,
        List<PostAttachment> attachments,
        List<Comment> comments,
        boolean liked,
        boolean scraped,
        boolean owner
) {
}
