package com.prevelio.vehicle.application.mapper;

import com.prevelio.vehicle.application.dto.VehicleRequestDto;
import com.prevelio.vehicle.application.dto.VehicleResponseDto;
import com.prevelio.vehicle.domain.model.Vehicle;

public class VehicleMapper {

    private VehicleMapper() {
        /* Utility class */
    }

    public static Vehicle toDomain(VehicleRequestDto dto) {
        Vehicle vehicle = new Vehicle();
        vehicle.setCustomerUuid(dto.getCustomerUuid());
        vehicle.setMake(dto.getMake());
        vehicle.setModel(dto.getModel());
        vehicle.setYear(dto.getYear());
        vehicle.setLicensePlate(dto.getLicensePlate());
        vehicle.setVin(dto.getVin());
        vehicle.setColor(dto.getColor());
        vehicle.setActive(dto.isActive());
        return vehicle;
    }

    public static VehicleResponseDto toDto(Vehicle vehicle) {
        VehicleResponseDto dto = new VehicleResponseDto();
        dto.setId(vehicle.getId());
        dto.setVehicleUuid(vehicle.getVehicleUuid());
        dto.setCustomerUuid(vehicle.getCustomerUuid());
        dto.setMake(vehicle.getMake());
        dto.setModel(vehicle.getModel());
        dto.setYear(vehicle.getYear());
        dto.setLicensePlate(vehicle.getLicensePlate());
        dto.setVin(vehicle.getVin());
        dto.setColor(vehicle.getColor());
        dto.setActive(vehicle.isActive());
        return dto;
    }
}
