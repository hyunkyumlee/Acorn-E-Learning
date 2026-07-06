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
    public int postCount() {
        return myPosts == null ? 0 : myPosts.size();
    }

    public int commentCount() {
        return myComments == null ? 0 : myComments.size();
    }

    public int likedCount() {
        return likedPosts == null ? 0 : likedPosts.size();
    }

    public int scrapCount() {
        return scrapedPosts == null ? 0 : scrapedPosts.size();
    }

    public int receivedLikeCount() {
        if (myPosts == null) {
            return 0;
        }
        return myPosts.stream()
                .map(CommunityPost::getLikeCount)
                .filter(count -> count != null)
                .mapToInt(Integer::intValue)
                .sum();
    }

    public int receivedCommentCount() {
        if (myPosts == null) {
            return 0;
        }
        return myPosts.stream()
                .map(CommunityPost::getCommentCount)
                .filter(count -> count != null)
                .mapToInt(Integer::intValue)
                .sum();
    }

    public int activityCount() {
        return postCount() + commentCount() + likedCount() + scrapCount();
    }
}
