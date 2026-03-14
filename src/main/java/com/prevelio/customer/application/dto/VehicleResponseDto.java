package com.prevelio.customer.application.dto;

import java.util.UUID;

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
public class VehicleResponseDto {
    private Long id;
    private UUID vehicleUuid;
    private String make;
    private String model;
    private Integer year;
    private String licensePlate;
    private String vin;
    private String color;
}
