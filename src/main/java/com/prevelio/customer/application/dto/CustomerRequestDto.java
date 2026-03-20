package com.prevelio.customer.application.dto;

import com.prevelio.customer.domain.model.CustomerStatus;

public record CustomerRequestDto(
    String name,
    String email,
    String phone,
    String address,
    String city,
    String state,
    String zip,
    String country,
    CustomerStatus status
) {}
