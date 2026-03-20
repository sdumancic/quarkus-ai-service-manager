package com.prevelio.appointment.application.mapper;

import com.prevelio.appointment.application.dto.AppointmentRequestDto;
import com.prevelio.appointment.application.dto.AppointmentResponseDto;
import com.prevelio.appointment.application.dto.StoredTireDto;
import com.prevelio.appointment.domain.model.Appointment;
import com.prevelio.appointment.domain.model.StoredTire;

public class AppointmentMapper {
    
    private AppointmentMapper() {
        // Utility class
    }

    public static Appointment toDomain(AppointmentRequestDto dto) {
        Appointment model = new Appointment();
        model.setCustomerUuid(dto.customerUuid());
        model.setVehicleUuid(dto.vehicleUuid());
        model.setStartDate(dto.startDate());
        model.setEndDate(dto.endDate());
        if (dto.serviceIds() != null) {
            model.setServiceIds(dto.serviceIds());
        }
        if (dto.storedTires() != null) {
            model.setStoredTires(dto.storedTires().stream()
                    .map(AppointmentMapper::toStoredTire)
                    .toList());
        }
        return model;
    }

    public static AppointmentResponseDto toDto(Appointment model) {
        return new AppointmentResponseDto(
            model.getId(),
            model.getCustomerId(),
            model.getCustomerUuid(),
            model.getVehicleId(),
            model.getVehicleUuid(),
            null, // vehicle is enriched later in service layer
            model.getStartDate(),
            model.getEndDate(),
            model.getServiceIds(),
            model.getStoredTires() != null ? model.getStoredTires().stream()
                    .map(AppointmentMapper::toStoredTireDto)
                    .toList() : null,
            model.getStatus() != null ? model.getStatus().name() : null
        );
    }

    private static StoredTire toStoredTire(StoredTireDto dto) {
        return new StoredTire(
                dto.brand(), dto.model(), dto.width(),
                dto.aspectRatio(), dto.diameter(),
                dto.season(), dto.condition()
        );
    }

    private static StoredTireDto toStoredTireDto(StoredTire model) {
        return new StoredTireDto(
                model.getBrand(),
                model.getModel(),
                model.getWidth(),
                model.getAspectRatio(),
                model.getDiameter(),
                model.getSeason(),
                model.getCondition()
        );
    }
}
