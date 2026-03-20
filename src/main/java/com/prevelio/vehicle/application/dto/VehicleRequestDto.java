package com.prevelio.vehicle.application.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VehicleRequestDto(
    @NotNull(message = "Customer UUID cannot be null")
    UUID customerUuid,
    
    @NotBlank(message = "Make cannot be blank")
    String make,
    
    @NotBlank(message = "Model cannot be blank")
    String model,
    
    Integer year,
    
    @NotBlank(message = "License plate cannot be blank")
    String licensePlate,
    
    String vin,
    String color,
    boolean active
) {
    // Convenience constructor to match previous default value of active=true
    public VehicleRequestDto(UUID customerUuid, String make, String model, Integer year, String licensePlate, String vin, String color) {
        this(customerUuid, make, model, year, licensePlate, vin, color, true);
    }
}
