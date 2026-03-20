package com.prevelio.service.application.dto;

import java.math.BigDecimal;

public record ServiceItemRequestDto(
    String code,
    String description,
    BigDecimal price,
    Integer durationInMinutes
) {}
