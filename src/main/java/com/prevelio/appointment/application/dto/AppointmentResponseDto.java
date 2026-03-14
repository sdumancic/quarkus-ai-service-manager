package com.prevelio.appointment.application.dto;

import java.time.LocalDateTime;
import java.util.List;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppointmentResponseDto {
    private Long id;
    private Long customerId;
    private UUID customerUuid;
    private Long vehicleId;
    private UUID vehicleUuid;
    private com.prevelio.customer.application.dto.VehicleResponseDto vehicle;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<Long> serviceIds;
    private List<StoredTireDto> storedTires;
    private String status;
}
