package com.acorn.elearning.common.response;

import java.util.List;

public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages, boolean last) {
    public static <T> PageResponse<T> empty() { return new PageResponse<>(List.of(), 0, 20, 0, 0, true); }
    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new PageResponse<>(content, page, size, totalElements, totalPages, totalPages == 0 || page + 1 >= totalPages);
    }
}
