package com.prevelio.vehicle.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.prevelio.vehicle.domain.model.Vehicle;

public interface VehicleRepository {
    Vehicle save(Vehicle vehicle);
    Vehicle update(Vehicle vehicle);
    Optional<Vehicle> findById(Long id);
    Optional<Vehicle> findByUuid(UUID uuid);
    List<Vehicle> findByCustomerUuid(UUID customerUuid);
    void delete(Long id);
}
