package com.prevelio.appointment.domain.model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Appointment {
    private Long id;
    private Long customerId;
    private UUID customerUuid;
    private Long vehicleId;
    private UUID vehicleUuid;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<Long> serviceIds = new ArrayList<>();
    private List<StoredTire> storedTires = new ArrayList<>();
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    public void cancel(LocalDateTime cancelDate) {
        if (this.status == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Appointment is already cancelled");
        }
        
        long daysBetween = ChronoUnit.DAYS.between(cancelDate, this.startDate);
        if (daysBetween < 2) {
            throw new IllegalStateException("Appointment cannot be cancelled less than 2 days before start date");
        }
        
        this.status = AppointmentStatus.CANCELLED;
    }
}
