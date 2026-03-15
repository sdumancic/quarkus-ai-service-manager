package com.prevelio.vehicle.application.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VehicleRequestDto {
    @NotNull(message = "Customer UUID cannot be null")
    private UUID customerUuid;
    
    @NotBlank(message = "Make cannot be blank")
    private String make;
    
    @NotBlank(message = "Model cannot be blank")
    private String model;
    
    private Integer year;
    
    @NotBlank(message = "License plate cannot be blank")
    private String licensePlate;
    
    private String vin;
    private String color;
    private boolean active = true;
}
