package com.prevelio.vehicle.application.dto;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VehicleResponseDto {
    private Long id;
    private UUID vehicleUuid;
    private UUID customerUuid;
    private String make;
    private String model;
    private Integer year;
    private String licensePlate;
    private String vin;
    private String color;
    private boolean active;
}
