package com.prevelio.vehicle.infrastructure.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.prevelio.vehicle.domain.model.Vehicle;
import com.prevelio.vehicle.domain.repository.VehicleRepository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InMemoryVehicleRepository implements VehicleRepository {

    private final Map<Long, Vehicle> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @Override
    public Vehicle save(Vehicle vehicle) {
        if (vehicle.getId() == null) {
            vehicle.setId(idGenerator.incrementAndGet());
        }
        if (vehicle.getVehicleUuid() == null) {
            vehicle.setVehicleUuid(UUID.randomUUID());
        }
        storage.put(vehicle.getId(), vehicle);
        return vehicle;
    }

    @Override
    public Vehicle update(Vehicle vehicle) {
        storage.put(vehicle.getId(), vehicle);
        return vehicle;
    }

    @Override
    public Optional<Vehicle> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<Vehicle> findByUuid(UUID uuid) {
        return storage.values().stream()
                .filter(v -> uuid.equals(v.getVehicleUuid()))
                .findFirst();
    }

    @Override
    public List<Vehicle> findByCustomerUuid(UUID customerUuid) {
        return storage.values().stream()
                .filter(v -> customerUuid.equals(v.getCustomerUuid()))
                .toList();
    }

    @Override
    public void delete(Long id) {
        storage.remove(id);
    }
}
