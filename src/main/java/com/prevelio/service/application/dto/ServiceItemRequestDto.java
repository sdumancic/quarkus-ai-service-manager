package com.prevelio.service.application.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceItemRequestDto {
    private String code;
    private String description;
    private BigDecimal price;
    private Integer durationInMinutes;
}
