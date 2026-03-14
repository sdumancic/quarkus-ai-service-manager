package com.prevelio.service.domain.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServiceItem {
    private Long id;
    private String code; // e.g., TIRE_CHANGE_16
    private String description;
    private BigDecimal price;
}
