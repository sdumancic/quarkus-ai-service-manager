package com.prevelio.vehicle.application.dto;

import java.util.UUID;

public record VehicleResponseDto(
    Long id,
    UUID vehicleUuid,
    UUID customerUuid,
    String make,
    String model,
    Integer year,
    String licensePlate,
    String vin,
    String color,
    boolean active
) {}
