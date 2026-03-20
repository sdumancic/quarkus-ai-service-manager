package com.prevelio.common.dto;

import java.util.List;

public record PagedResponse<T>(
    List<T> data,
    PageMetadata metadata
) {}
