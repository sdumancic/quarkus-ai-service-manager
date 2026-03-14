package com.prevelio.appointment.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentRequestDto {
    @NotNull(message = "Customer UUID is required")
    private UUID customerUuid;
    
    @NotNull(message = "Vehicle UUID is required")
    private UUID vehicleUuid;
    
    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;
    
    @NotNull(message = "End date is required")
    private LocalDateTime endDate;
    
    @NotEmpty(message = "At least one service must be selected")
    private List<Long> serviceIds;
    private List<StoredTireDto> storedTires;
}
