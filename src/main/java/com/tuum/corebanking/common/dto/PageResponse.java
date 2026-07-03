package com.tuum.corebanking.common.dto;

import java.util.List;

public record PageResponse<T>(
        List<T> data,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public PageResponse(List<T> data, int page, int size, long totalElements) {
        this(data, page, size, totalElements, (int) Math.ceil((double) totalElements / size));
    }
}
