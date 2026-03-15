package com.prevelio.vehicle.application.service;

import java.util.List;
import java.util.UUID;

import com.prevelio.customer.application.service.CustomerService;
import com.prevelio.vehicle.application.dto.VehicleRequestDto;
import com.prevelio.vehicle.application.dto.VehicleResponseDto;
import com.prevelio.vehicle.application.mapper.VehicleMapper;
import com.prevelio.vehicle.domain.model.Vehicle;
import com.prevelio.vehicle.domain.repository.VehicleRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class VehicleAppService {

    private static final String VEHICLE_NOT_FOUND = "Vehicle not found";
    private final VehicleRepository vehicleRepository;
    private final CustomerService customerService;

    @Inject
    public VehicleAppService(VehicleRepository vehicleRepository, CustomerService customerService) {
        this.vehicleRepository = vehicleRepository;
        this.customerService = customerService;
    }

    public List<VehicleResponseDto> getVehiclesByCustomerUuid(UUID customerUuid) {
        // Validate customer exists
        customerService.getCustomerByUuid(customerUuid);
        
        return vehicleRepository.findByCustomerUuid(customerUuid).stream()
                .map(VehicleMapper::toDto)
                .toList();
    }

    public VehicleResponseDto createVehicle(VehicleRequestDto request) {
        // Validate customer exists and link IDs
        var customer = customerService.getCustomerByUuid(request.getCustomerUuid());
        
        Vehicle vehicle = VehicleMapper.toDomain(request);
        vehicle.setCustomerId(customer.getId());
        
        Vehicle saved = vehicleRepository.save(vehicle);
        return VehicleMapper.toDto(saved);
    }

    public VehicleResponseDto updateVehicle(UUID vehicleUuid, VehicleRequestDto request) {
        Vehicle existing = vehicleRepository.findByUuid(vehicleUuid)
                .orElseThrow(() -> new NotFoundException(VEHICLE_NOT_FOUND));
        
        // Ensure customer is valid
        var customer = customerService.getCustomerByUuid(request.getCustomerUuid());
        
        existing.setCustomerUuid(request.getCustomerUuid());
        existing.setCustomerId(customer.getId());
        existing.setMake(request.getMake());
        existing.setModel(request.getModel());
        existing.setYear(request.getYear());
        existing.setLicensePlate(request.getLicensePlate());
        existing.setVin(request.getVin());
        existing.setColor(request.getColor());
        existing.setActive(request.isActive());
        
        Vehicle updated = vehicleRepository.update(existing);
        return VehicleMapper.toDto(updated);
    }

    public void disableVehicle(UUID vehicleUuid) {
        Vehicle existing = vehicleRepository.findByUuid(vehicleUuid)
                .orElseThrow(() -> new NotFoundException(VEHICLE_NOT_FOUND));
        
        existing.setActive(false);
        vehicleRepository.update(existing);
    }

    public VehicleResponseDto getVehicleByUuid(UUID vehicleUuid) {
        return vehicleRepository.findByUuid(vehicleUuid)
                .map(VehicleMapper::toDto)
                .orElseThrow(() -> new NotFoundException(VEHICLE_NOT_FOUND));
    }
    
    public Vehicle getVehicleModelByUuid(UUID vehicleUuid) {
        return vehicleRepository.findByUuid(vehicleUuid)
                .orElseThrow(() -> new NotFoundException(VEHICLE_NOT_FOUND));
    }
}
