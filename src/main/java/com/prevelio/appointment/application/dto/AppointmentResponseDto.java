package com.prevelio.appointment.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AppointmentResponseDto(
    Long id,
    Long customerId,
    UUID customerUuid,
    Long vehicleId,
    UUID vehicleUuid,
    com.prevelio.vehicle.application.dto.VehicleResponseDto vehicle,
    LocalDateTime startDate,
    LocalDateTime endDate,
    List<Long> serviceIds,
    List<StoredTireDto> storedTires,
    String status
) {
    public AppointmentResponseDto withVehicle(com.prevelio.vehicle.application.dto.VehicleResponseDto vehicle) {
        return new AppointmentResponseDto(id, customerId, customerUuid, vehicleId, vehicleUuid, vehicle, startDate, endDate, serviceIds, storedTires, status);
    }
}
