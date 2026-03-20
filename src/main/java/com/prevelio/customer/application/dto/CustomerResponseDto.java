package com.prevelio.customer.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.prevelio.customer.domain.model.CustomerStatus;

public record CustomerResponseDto(
    Long id,
    String name,
    String email,
    String phone,
    String address,
    String city,
    String state,
    String zip,
    String country,
    CustomerStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    UUID customerUuid
) {}
