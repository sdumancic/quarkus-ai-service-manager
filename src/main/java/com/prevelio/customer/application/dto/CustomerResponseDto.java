package com.prevelio.customer.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.prevelio.customer.domain.model.CustomerStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CustomerResponseDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String zip;
    private String country;
    private CustomerStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID customerUuid;
    private List<VehicleResponseDto> vehicles;
}
