package com.bfb.interfaces.rest.common.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic wrapper for paginated API responses.
 * Provides a consistent structure for all paginated endpoints.
 * 
 * @param <T> the type of content in the page
 */
public record PageResponse<T>(
    List<T> content,
    int pageNumber,
    int pageSize,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last,
    boolean empty
) {
    /**
     * Creates a PageResponse from a Spring Data Page.
     * 
     * @param page the Spring Data page
     * @param <T> the content type
     * @return a PageResponse instance
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast(),
            page.isEmpty()
        );
    }
}
