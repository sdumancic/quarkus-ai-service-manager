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
        model.setCustomerUuid(dto.getCustomerUuid());
        model.setVehicleUuid(dto.getVehicleUuid());
        model.setStartDate(dto.getStartDate());
        model.setEndDate(dto.getEndDate());
        if (dto.getServiceIds() != null) {
            model.setServiceIds(dto.getServiceIds());
        }
        if (dto.getStoredTires() != null) {
            model.setStoredTires(dto.getStoredTires().stream()
                    .map(AppointmentMapper::toStoredTire)
                    .toList());
        }
        return model;
    }

    public static AppointmentResponseDto toDto(Appointment model) {
        AppointmentResponseDto dto = new AppointmentResponseDto();
        dto.setId(model.getId());
        dto.setCustomerId(model.getCustomerId());
        dto.setCustomerUuid(model.getCustomerUuid());
        dto.setVehicleId(model.getVehicleId());
        dto.setVehicleUuid(model.getVehicleUuid());
        dto.setStartDate(model.getStartDate());
        dto.setEndDate(model.getEndDate());
        dto.setServiceIds(model.getServiceIds());
        dto.setStatus(model.getStatus().name());
        if (model.getStoredTires() != null) {
            dto.setStoredTires(model.getStoredTires().stream()
                    .map(AppointmentMapper::toStoredTireDto)
                    .toList());
        }
        return dto;
    }

    private static StoredTire toStoredTire(StoredTireDto dto) {
        return new StoredTire(
                dto.getBrand(), dto.getModel(), dto.getWidth(),
                dto.getAspectRatio(), dto.getDiameter(),
                dto.getSeason(), dto.getCondition()
        );
    }

    private static StoredTireDto toStoredTireDto(StoredTire model) {
        StoredTireDto dto = new StoredTireDto();
        dto.setBrand(model.getBrand());
        dto.setModel(model.getModel());
        dto.setWidth(model.getWidth());
        dto.setAspectRatio(model.getAspectRatio());
        dto.setDiameter(model.getDiameter());
        dto.setSeason(model.getSeason());
        dto.setCondition(model.getCondition());
        return dto;
    }
}
