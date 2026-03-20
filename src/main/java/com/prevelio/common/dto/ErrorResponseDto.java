package com.prevelio.common.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponseDto(
    String message,
    int status,
    LocalDateTime timestamp,
    List<String> details
) {
    public ErrorResponseDto(String message, int status) {
        this(message, status, LocalDateTime.now(), null);
    }
}
