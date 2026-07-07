package com.acorn.elearning.user.dto.response;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

public record CommunityActivityPageResponse(
        String type,
        String path,
        String title,
        String description,
        String markerClass,
        String communityUrl,
        String selectedCategory,
        String query,
        int page,
        int totalPages,
        boolean first,
        boolean last,
        String previousUrl,
        String nextUrl,
        List<FilterItem> filters,
        List<PageItem> pageNumbers,
        List<PostItem> items,
        boolean empty
) {
    private static final int PAGE_SIZE = 10;

    public static CommunityActivityPageResponse of(String type, String category, String query, int page, List<PostItem> items) {
        String safeType = normalizeType(type);
        String safeCategory = normalizeCategory(category);
        String safeQuery = query == null ? "" : query.trim();
        PageConfig config = config(safeType);
        List<PostItem> sourceItems = items == null ? List.of() : items;
        List<PostItem> filteredItems = sourceItems.stream()
                .filter(item -> "ALL".equals(safeCategory) || item.category().equals(safeCategory))
                .filter(item -> matches(item, safeQuery))
                .toList();
        int totalPages = Math.max(1, (int) Math.ceil(filteredItems.size() / (double) PAGE_SIZE));
        int safePage = Math.min(Math.max(page, 0), totalPages - 1);
        int fromIndex = Math.min(safePage * PAGE_SIZE, filteredItems.size());
        int toIndex = Math.min(fromIndex + PAGE_SIZE, filteredItems.size());
        List<PostItem> pageItems = filteredItems.subList(fromIndex, toIndex);

        return new CommunityActivityPageResponse(
                safeType,
                config.path(),
                config.title(),
                config.description(),
                config.markerClass(),
                "/community/board",
                safeCategory,
                safeQuery,
                safePage,
                totalPages,
                safePage == 0,
                safePage >= totalPages - 1,
                url(config.path(), safeCategory, safeQuery, Math.max(safePage - 1, 0)),
                url(config.path(), safeCategory, safeQuery, Math.min(safePage + 1, totalPages - 1)),
                filters(config.path(), safeCategory, safeQuery),
                pageNumbers(config.path(), safeCategory, safeQuery, totalPages, safePage),
                pageItems,
                pageItems.isEmpty()
        );
    }

    private static List<FilterItem> filters(String path, String selectedCategory, String query) {
        return List.of(
                filter(path, "ALL", "전체", selectedCategory, query),
                filter(path, "FREE", "자유", selectedCategory, query),
                filter(path, "QUESTION", "질문", selectedCategory, query),
                filter(path, "STUDY_LOG", "공부 일지", selectedCategory, query)
        );
    }

    private static FilterItem filter(String path, String category, String label, String selectedCategory, String query) {
        return new FilterItem(category, label, category.equals(selectedCategory), url(path, category, query, 0));
    }

    private static List<PageItem> pageNumbers(String path, String category, String query, int totalPages, int currentPage) {
        return java.util.stream.IntStream.range(0, totalPages)
                .mapToObj(page -> new PageItem(page, page + 1, page == currentPage, url(path, category, query, page)))
                .toList();
    }

    private static boolean matches(PostItem item, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }
        String loweredQuery = query.toLowerCase(Locale.ROOT);
        return item.title().toLowerCase(Locale.ROOT).contains(loweredQuery)
                || safeText(item.excerpt()).toLowerCase(Locale.ROOT).contains(loweredQuery);
    }

    private static String normalizeType(String type) {
        if ("SCRAPS".equalsIgnoreCase(type) || "POSTS".equalsIgnoreCase(type)) {
            return type.toUpperCase(Locale.ROOT);
        }
        return "LIKED";
    }

    private static String normalizeCategory(String category) {
        if ("INFO".equalsIgnoreCase(category)) {
            return "STUDY_LOG";
        }
        if ("FREE".equalsIgnoreCase(category) || "QUESTION".equalsIgnoreCase(category) || "STUDY_LOG".equalsIgnoreCase(category)) {
            return category.toUpperCase(Locale.ROOT);
        }
        return "ALL";
    }

    private static PageConfig config(String type) {
        return switch (type) {
            case "SCRAPS" -> new PageConfig(
                    "/mypage/community/scraps",
                    "스크랩한 글",
                    "나중에 다시 볼 게시글을 모아두었어요.",
                    "bookmark"
            );
            case "POSTS" -> new PageConfig(
                    "/mypage/community/posts",
                    "작성한 글",
                    "내가 커뮤니티에 작성한 글을 확인하세요.",
                    "button"
            );
            default -> new PageConfig(
                    "/mypage/community/liked",
                    "좋아요 누른 글",
                    "관심 있게 본 게시글을 다시 확인하세요.",
                    "heart"
            );
        };
    }

    private static String url(String path, String category, String query, int page) {
        return path + "?category=" + encode(category) + "&q=" + encode(query == null ? "" : query) + "&page=" + page;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String safeText(String value) {
        return value == null ? "" : value;
    }

    private record PageConfig(String path, String title, String description, String markerClass) {
    }

    public record FilterItem(String category, String label, boolean active, String url) {
    }

    public record PageItem(int page, int label, boolean active, String url) {
    }

    public record PostItem(
            Long postId,
            String category,
            String categoryLabel,
            String title,
            String excerpt,
            String boardLabel,
            String authorLabel,
            String dateLabel,
            int likeCount,
            int commentCount,
            String detailUrl
    ) {
    }
}
