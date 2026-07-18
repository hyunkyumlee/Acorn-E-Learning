package com.acorn.elearning.community.dto.response;

import com.acorn.elearning.community.model.Comment;
import com.acorn.elearning.community.model.CommunityPost;
import java.time.LocalDateTime;
import java.util.Comparator;
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

    public List<CommunityPost> filteredMyPosts(Long subjectId, String sort, String direction) {
        if (myPosts == null) {
            return List.of();
        }
        Comparator<CommunityPost> comparator = profilePostComparator(sort);
        if (!"asc".equals(direction)) {
            comparator = comparator.reversed();
        }
        return myPosts.stream()
                .filter(post -> subjectId == null || subjectId.equals(post.getSubjectId()))
                .sorted(comparator)
                .toList();
    }

    private static Comparator<CommunityPost> profilePostComparator(String sort) {
        Comparator<Integer> countComparator = Comparator.nullsLast(Comparator.naturalOrder());
        Comparator<Long> idComparator = Comparator.nullsLast(Comparator.naturalOrder());
        Comparator<LocalDateTime> createdAtComparator = Comparator.nullsLast(Comparator.naturalOrder());
        Comparator<CommunityPost> comparator = switch (sort) {
            case "views" -> Comparator.comparing(CommunityPost::getViewCount, countComparator);
            case "popular" -> Comparator.comparing(CommunityPost::getLikeCount, countComparator)
                    .thenComparing(CommunityPost::getCommentCount, countComparator)
                    .thenComparing(CommunityPost::getScrapCount, countComparator);
            default -> Comparator.comparing(CommunityPost::getCreatedAt, createdAtComparator);
        };
        return comparator.thenComparing(CommunityPost::getPostId, idComparator);
    }
}
