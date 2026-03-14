package com.prevelio.appointment.application.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import com.prevelio.appointment.application.dto.AppointmentRequestDto;
import com.prevelio.appointment.application.dto.AppointmentResponseDto;
import com.prevelio.appointment.application.mapper.AppointmentMapper;
import com.prevelio.appointment.domain.model.Appointment;
import com.prevelio.appointment.domain.repository.AppointmentRepository;
import com.prevelio.service.domain.repository.ServiceRepository;

import com.prevelio.common.lock.DistributedLock;
import com.prevelio.customer.application.mapper.CustomerMapper;
import com.prevelio.customer.application.service.CustomerService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class AppointmentAppService {

    private static final String APPOINTMENT_NOT_FOUND = "Appointment not found";
    private final AppointmentRepository repository;
    private final CustomerService customerService;
    private final ServiceRepository serviceRepository;

    @Inject
    public AppointmentAppService(AppointmentRepository repository, CustomerService customerService,
            ServiceRepository serviceRepository) {
        this.repository = repository;
        this.customerService = customerService;
        this.serviceRepository = serviceRepository;
    }

    public List<AppointmentResponseDto> getAllAppointments() {
        return repository.findAll().stream()
                .map(AppointmentMapper::toDto)
                .map(this::enrichWithVehicleInfo)
                .toList();
    }

    public List<AppointmentResponseDto> getActiveAppointmentsByCustomerId(Long customerId) {
        return repository.findActiveByCustomerId(customerId).stream()
                .map(AppointmentMapper::toDto)
                .map(this::enrichWithVehicleInfo)
                .toList();
    }

    public AppointmentResponseDto getAppointmentById(Long id) {
        return repository.findById(id)
                .map(AppointmentMapper::toDto)
                .map(this::enrichWithVehicleInfo)
                .orElseThrow(() -> new NotFoundException(APPOINTMENT_NOT_FOUND));
    }

    @DistributedLock(key = "create-appointment")
    public AppointmentResponseDto createAppointment(AppointmentRequestDto request) {
        validateRequest(request);
        Appointment model = AppointmentMapper.toDomain(request);

        // Resolve customerId and vehicleId
        if (request.getCustomerUuid() != null) {
            try {
                Long customerId = customerService.getCustomerByUuid(request.getCustomerUuid()).getId();
                model.setCustomerId(customerId);

                if (request.getVehicleUuid() != null) {
                    com.prevelio.customer.domain.model.Vehicle vehicle = customerService
                            .getVehicleByUuid(request.getCustomerUuid(), request.getVehicleUuid());
                    model.setVehicleId(vehicle.getId());
                }
            } catch (NotFoundException e) {
                throw new BadRequestException(e.getMessage());
            }
        }

        Appointment saved = repository.save(model);
        return enrichWithVehicleInfo(AppointmentMapper.toDto(saved));
    }

    public AppointmentResponseDto updateAppointment(Long id, AppointmentRequestDto request) {
        validateRequest(request);
        Appointment existing = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(APPOINTMENT_NOT_FOUND));

        Appointment updatedModel = AppointmentMapper.toDomain(request);

        if (request.getCustomerUuid() != null) {
            try {
                Long customerId = customerService.getCustomerByUuid(request.getCustomerUuid()).getId();
                existing.setCustomerId(customerId);
                existing.setCustomerUuid(request.getCustomerUuid());

                if (request.getVehicleUuid() != null) {
                    com.prevelio.customer.domain.model.Vehicle vehicle = customerService
                            .getVehicleByUuid(request.getCustomerUuid(), request.getVehicleUuid());
                    existing.setVehicleId(vehicle.getId());
                    existing.setVehicleUuid(request.getVehicleUuid());
                }
            } catch (NotFoundException e) {
                throw new BadRequestException(e.getMessage());
            }
        }

        existing.setStartDate(updatedModel.getStartDate());
        existing.setEndDate(updatedModel.getEndDate());
        existing.setServiceIds(updatedModel.getServiceIds());
        existing.setStoredTires(updatedModel.getStoredTires());
        // Status remains unchanged through updates

        Appointment saved = repository.update(existing);
        return enrichWithVehicleInfo(AppointmentMapper.toDto(saved));
    }

    public void cancelAppointment(Long id) {
        Appointment existing = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(APPOINTMENT_NOT_FOUND));

        try {
            existing.cancel(LocalDateTime.now());
        } catch (IllegalStateException e) {
            throw new BadRequestException(e.getMessage());
        }

        repository.update(existing);
    }

    public void deleteAppointment(Long id) {
        if (repository.findById(id).isEmpty()) {
            throw new NotFoundException(APPOINTMENT_NOT_FOUND);
        }
        repository.delete(id);
    }

    private AppointmentResponseDto enrichWithVehicleInfo(AppointmentResponseDto dto) {
        if (dto.getCustomerUuid() != null && dto.getVehicleUuid() != null) {
            try {
                com.prevelio.customer.domain.model.Vehicle vehicle = customerService.getVehicleByUuid(
                        dto.getCustomerUuid(),
                        dto.getVehicleUuid());
                dto.setVehicle(CustomerMapper.toVehicleResponseDto(vehicle));
            } catch (NotFoundException e) {
                // Vehicle might have been deleted or UUID changed, leave null
            }
        }
        return dto;
    }

    private void validateRequest(AppointmentRequestDto request) {
        if (request.getEndDate().isBefore(request.getStartDate())
                || request.getEndDate().isEqual(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        // Check 30-minute slots
        if (!isRoundedToHalfHour(request.getStartDate())) {
            throw new BadRequestException("Start date must be rounded to half hour (e.g. 10:00, 10:30)");
        }
        if (!isRoundedToHalfHour(request.getEndDate())) {
            throw new BadRequestException("End date must be rounded to half hour (e.g. 10:00, 10:30)");
        }

        // Check duration matches sum of services
        long expectedDurationMinutes = 0;
        if (request.getServiceIds() != null) {
            for (Long serviceId : request.getServiceIds()) {
                expectedDurationMinutes += serviceRepository.findById(serviceId)
                        .orElseThrow(() -> new BadRequestException("Service item not found: " + serviceId))
                        .getDurationInMinutes();
            }
        }

        long actualDurationMinutes = Duration.between(request.getStartDate(), request.getEndDate()).toMinutes();
        if (actualDurationMinutes != expectedDurationMinutes) {
            throw new BadRequestException("Appointment duration (" + actualDurationMinutes 
                + " min) does not match required service duration (" + expectedDurationMinutes + " min)");
        }

        LocalDateTime minAllowedDate = LocalDateTime.now().plusDays(3);
        if (request.getEndDate().isBefore(minAllowedDate)) {
            throw new BadRequestException("Appointments cannot be scheduled or end within the next 3 days");
        }
        if (request.getStartDate().isBefore(minAllowedDate)) {
            throw new BadRequestException("Appointments cannot be scheduled or start within the next 3 days");
        }
    }

    private boolean isRoundedToHalfHour(LocalDateTime dateTime) {
        return (dateTime.getMinute() == 0 || dateTime.getMinute() == 30) 
            && dateTime.getSecond() == 0 
            && dateTime.getNano() == 0;
    }
}
