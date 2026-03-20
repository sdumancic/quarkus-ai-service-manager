package com.prevelio.common.dto;

public record PageMetadata(
    int pageSize,
    long totalItems,
    int pageIndex
) {}
