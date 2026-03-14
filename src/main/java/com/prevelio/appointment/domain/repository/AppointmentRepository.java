package com.prevelio.appointment.domain.repository;

import java.util.List;
import java.util.Optional;

import com.prevelio.appointment.domain.model.Appointment;

public interface AppointmentRepository {
    Appointment save(Appointment appointment);
    Appointment update(Appointment appointment);
    Optional<Appointment> findById(Long id);
    List<Appointment> findActiveByCustomerId(Long customerId);
    List<Appointment> findAll();
    void delete(Long id);
}
