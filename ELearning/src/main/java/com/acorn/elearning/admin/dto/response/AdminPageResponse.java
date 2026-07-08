package com.acorn.elearning.admin.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class AdminPageResponse <T>{
    private final List<T> items;
    private final int currentPage;
    private final int pageSize;
    private final long totalCount;
    private final int totalPages;
    private final boolean hasPrevious;
    private final boolean hasNext;

    public AdminPageResponse(List<T> items, int currentPage, int pageSize, long totalCount) {
        this.items = items;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
        this.totalPages = (int) Math.ceil((double) totalCount / pageSize);
        this.hasPrevious = currentPage > 1;
        this.hasNext = currentPage < totalPages;
    }

}
