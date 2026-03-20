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
        vehicle.setCustomerUuid(dto.customerUuid());
        vehicle.setMake(dto.make());
        vehicle.setModel(dto.model());
        vehicle.setYear(dto.year());
        vehicle.setLicensePlate(dto.licensePlate());
        vehicle.setVin(dto.vin());
        vehicle.setColor(dto.color());
        vehicle.setActive(dto.active());
        return vehicle;
    }

    public static VehicleResponseDto toDto(Vehicle vehicle) {
        return new VehicleResponseDto(
            vehicle.getId(),
            vehicle.getVehicleUuid(),
            vehicle.getCustomerUuid(),
            vehicle.getMake(),
            vehicle.getModel(),
            vehicle.getYear(),
            vehicle.getLicensePlate(),
            vehicle.getVin(),
            vehicle.getColor(),
            vehicle.isActive()
        );
    }
}
