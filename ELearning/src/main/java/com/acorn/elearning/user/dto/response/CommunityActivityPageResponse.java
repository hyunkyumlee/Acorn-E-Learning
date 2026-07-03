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
    private static final int PAGE_SIZE = 2;

    public static CommunityActivityPageResponse of(String type, String category, String query, int page) {
        String safeType = normalizeType(type);
        String safeCategory = normalizeCategory(category);
        String safeQuery = query == null ? "" : query.trim();
        PageConfig config = config(safeType);
        List<PostItem> filteredItems = samples(safeType).stream()
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
                filter(path, "INFO", "정보", selectedCategory, query)
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
                || item.excerpt().toLowerCase(Locale.ROOT).contains(loweredQuery);
    }

    private static String normalizeType(String type) {
        if ("SCRAPS".equalsIgnoreCase(type) || "POSTS".equalsIgnoreCase(type)) {
            return type.toUpperCase(Locale.ROOT);
        }
        return "LIKED";
    }

    private static String normalizeCategory(String category) {
        if ("FREE".equalsIgnoreCase(category) || "QUESTION".equalsIgnoreCase(category) || "INFO".equalsIgnoreCase(category)) {
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

    private static List<PostItem> samples(String type) {
        return switch (type) {
            case "SCRAPS" -> List.of(
                    post("QUESTION", "질문", "반복문이 헷갈려요", "for문과 while문의 차이를 잘 모르겠어요. 예시로 설명해주실 수 있을까요?", "Java 질문 게시판", "작성자", "2026.06.14", 12, 2),
                    post("FREE", "자유", "오늘 SQL 테스트 합격했어요!", "SQL Lv.5 입력 후기 남겨요! 모두 화이팅", "자유 게시판", "작성자", "2026.06.14", 8, 3),
                    post("INFO", "정보", "JOIN 정리 자료 공유", "INNER JOIN과 LEFT JOIN을 한 번에 비교해봤어요.", "정보 게시판", "작성자", "2026.06.12", 16, 5),
                    post("QUESTION", "질문", "Python 리스트 질문 있어요", "append와 extend 차이를 예제로 알고 싶어요.", "Python 질문 게시판", "작성자", "2026.06.10", 5, 4)
            );
            case "POSTS" -> List.of(
                    post("QUESTION", "질문", "Python 리스트 질문 있어요", "", "질문 게시판", "작성일", "2026.06.10", 5, 4),
                    post("FREE", "자유", "SQL Lv.5 합격 후기", "", "자유 게시판", "작성일", "2026.06.11", 10, 2),
                    post("INFO", "정보", "Java 컬렉션 정리", "", "정보 게시판", "작성일", "2026.06.12", 7, 1),
                    post("FREE", "자유", "오늘 학습 루틴 공유", "", "자유 게시판", "작성일", "2026.06.13", 3, 0)
            );
            default -> List.of(
                    post("QUESTION", "질문", "반복문이 헷갈려요", "for문과 while문의 차이를 잘 모르겠어요. 예시로 설명해주실 수 있을까요?", "Java 질문 게시판", "작성자", "2026.06.14", 12, 2),
                    post("FREE", "자유", "오늘 SQL 테스트 합격했어요!", "SQL Lv.5 입력 후기 남겨요! 모두 화이팅", "자유 게시판", "작성자", "2026.06.14", 8, 3),
                    post("INFO", "정보", "면접 질문 복습 자료", "자주 나오는 Java 기초 질문을 정리했어요.", "정보 게시판", "작성자", "2026.06.12", 9, 1),
                    post("FREE", "자유", "공부 일지 공유", "오늘은 SQL JOIN 문제를 풀었어요.", "자유 게시판", "작성자", "2026.06.11", 4, 2)
            );
        };
    }

    private static PostItem post(
            String category,
            String categoryLabel,
            String title,
            String excerpt,
            String boardLabel,
            String authorLabel,
            String dateLabel,
            int likeCount,
            int commentCount
    ) {
        return new PostItem(
                category,
                categoryLabel,
                title,
                excerpt,
                boardLabel,
                authorLabel,
                dateLabel,
                likeCount,
                commentCount,
                "/community/detail"
        );
    }

    private static String url(String path, String category, String query, int page) {
        return path + "?category=" + encode(category) + "&q=" + encode(query == null ? "" : query) + "&page=" + page;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private record PageConfig(String path, String title, String description, String markerClass) {
    }

    public record FilterItem(String category, String label, boolean active, String url) {
    }

    public record PageItem(int page, int label, boolean active, String url) {
    }

    public record PostItem(
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
