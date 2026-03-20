package com.prevelio.appointment.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record AppointmentRequestDto(
    @NotNull(message = "Customer UUID is required")
    UUID customerUuid,
    
    @NotNull(message = "Vehicle UUID is required")
    UUID vehicleUuid,
    
    @NotNull(message = "Start date is required")
    LocalDateTime startDate,
    
    LocalDateTime endDate,
    
    @NotEmpty(message = "At least one service must be selected")
    List<Long> serviceIds,
    
    List<StoredTireDto> storedTires
) {
    // Add a copy-with-new-endDate method if needed, or simply handle it elsewhere
    public AppointmentRequestDto withEndDate(LocalDateTime endDate) {
        return new AppointmentRequestDto(customerUuid, vehicleUuid, startDate, endDate, serviceIds, storedTires);
    }
}
