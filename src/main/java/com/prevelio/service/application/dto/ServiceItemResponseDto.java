package com.prevelio.service.application.dto;

import java.math.BigDecimal;

public record ServiceItemResponseDto(
    Long id,
    String code,
    String description,
    BigDecimal price,
    Integer durationInMinutes
) {}
