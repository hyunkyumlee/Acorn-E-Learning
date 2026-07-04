package com.acorn.elearning.community.dto.response;

import com.acorn.elearning.community.model.Comment;
import com.acorn.elearning.community.model.CommunityPost;
import java.util.List;

public record CommunityProfileResponse(
        List<CommunityPost> myPosts,
        List<Comment> myComments,
        List<CommunityPost> likedPosts,
        List<CommunityPost> scrapedPosts
) {
}
