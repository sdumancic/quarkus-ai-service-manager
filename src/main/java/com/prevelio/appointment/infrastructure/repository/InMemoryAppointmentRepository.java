package com.prevelio.appointment.infrastructure.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.prevelio.appointment.domain.model.Appointment;
import com.prevelio.appointment.domain.repository.AppointmentRepository;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InMemoryAppointmentRepository implements AppointmentRepository {

    private final Map<Long, Appointment> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @Override
    public Appointment save(Appointment appointment) {
        Long id = idGenerator.incrementAndGet();
        appointment.setId(id);
        storage.put(id, appointment);
        return appointment;
    }

    @Override
    public Appointment update(Appointment appointment) {
        storage.put(appointment.getId(), appointment);
        return appointment;
    }

    @Override
    public Optional<Appointment> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Appointment> findActiveByCustomerId(Long customerId) {
        return storage.values().stream()
                .filter(a -> a.getCustomerId().equals(customerId) && a.getStatus() == com.prevelio.appointment.domain.model.AppointmentStatus.SCHEDULED)
                .toList();
    }

    @Override
    public List<Appointment> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void delete(Long id) {
        storage.remove(id);
    }
}
