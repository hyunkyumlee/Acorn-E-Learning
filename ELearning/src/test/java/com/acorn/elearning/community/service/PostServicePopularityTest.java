package com.acorn.elearning.community.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.acorn.elearning.community.dto.response.PostPageResponse;
import com.acorn.elearning.community.form.PostSearchCondition;
import com.acorn.elearning.community.mapper.CommentMapper;
import com.acorn.elearning.community.mapper.CommunityPostMapper;
import com.acorn.elearning.community.mapper.PostAttachmentMapper;
import com.acorn.elearning.community.mapper.PostLikeMapper;
import com.acorn.elearning.community.mapper.PostScrapMapper;
import com.acorn.elearning.community.model.CommunityPost;
import java.lang.reflect.Proxy;
import java.util.List;
import org.junit.jupiter.api.Test;

class PostServicePopularityTest {

    @Test
    void page_marks_weekly_then_monthly_popular_posts() {
        CommunityPost weekly = post(1L);
        CommunityPost monthly = post(2L);
        CommunityPost normal = post(3L);
        PostService service = service(List.of(weekly, monthly, normal), List.of(1L), List.of(1L, 2L));

        PostPageResponse response = service.page(new PostSearchCondition());

        assertTrue(response.posts().get(0).isPopular());
        assertEquals("주간 인기", response.posts().get(0).getPopularLabel());
        assertTrue(response.posts().get(1).isPopular());
        assertEquals("월간 인기", response.posts().get(1).getPopularLabel());
        assertFalse(response.posts().get(2).isPopular());
    }

    private static PostService service(
            List<CommunityPost> posts,
            List<Long> weeklyPopularPostIds,
            List<Long> monthlyPopularPostIds
    ) {
        CommunityPostMapper postMapper = (CommunityPostMapper) Proxy.newProxyInstance(
                CommunityPostMapper.class.getClassLoader(),
                new Class<?>[]{CommunityPostMapper.class},
                (target, method, args) -> switch (method.getName()) {
                    case "findPage" -> posts;
                    case "countPage" -> (long) posts.size();
                    case "findWeeklyPopularPostIds" -> weeklyPopularPostIds;
                    case "findMonthlyPopularPostIds" -> monthlyPopularPostIds;
                    default -> throw new AssertionError(method.getName() + " mapper를 호출하면 안 됩니다.");
                });
        return new PostService(
                postMapper,
                unusedMapper(PostAttachmentMapper.class),
                unusedMapper(CommentMapper.class),
                unusedMapper(PostLikeMapper.class),
                unusedMapper(PostScrapMapper.class),
                null
        );
    }

    private static CommunityPost post(Long postId) {
        CommunityPost post = new CommunityPost();
        post.setPostId(postId);
        return post;
    }

    private static <T> T unusedMapper(Class<T> type) {
        Object proxy = Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                (target, method, args) -> {
                    throw new AssertionError(method.getName() + " mapper를 호출하면 안 됩니다.");
                });
        return type.cast(proxy);
    }
}
