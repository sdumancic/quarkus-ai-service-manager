package com.prevelio.appointment.infrastructure.config;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.prevelio.appointment.application.dto.AppointmentRequestDto;
import com.prevelio.appointment.application.service.AppointmentAppService;
import com.prevelio.appointment.domain.model.Appointment;
import com.prevelio.appointment.domain.model.AppointmentStatus;
import com.prevelio.appointment.domain.repository.AppointmentRepository;
import com.prevelio.common.dto.PagedResponse;
import com.prevelio.customer.application.dto.CustomerSearchCriteria;
import com.prevelio.customer.application.service.CustomerService;
import com.prevelio.customer.domain.model.Customer;
import com.prevelio.vehicle.application.dto.VehicleResponseDto;
import com.prevelio.vehicle.application.service.VehicleAppService;

import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.interceptor.Interceptor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class AppointmentStartup {

    private final AppointmentAppService appointmentAppService;
    private final CustomerService customerService;
    private final AppointmentRepository appointmentRepository;
    private final VehicleAppService vehicleAppService;

    public AppointmentStartup(AppointmentAppService appointmentAppService, CustomerService customerService,
            AppointmentRepository appointmentRepository, VehicleAppService vehicleAppService) {
        this.appointmentAppService = appointmentAppService;
        this.customerService = customerService;
        this.appointmentRepository = appointmentRepository;
        this.vehicleAppService = vehicleAppService;
    }

    // Set priority to run after CustomerStartup (which runs at default priority of
    // 2500)
    void onStart(@Observes @Priority(Interceptor.Priority.APPLICATION + 1000) StartupEvent ev) {
        log.info("Generating sample appointments for customers...");

        // Fetch all customers using an empty criteria
        CustomerSearchCriteria emptyCriteria = new CustomerSearchCriteria(null, null, 0, 100);
        PagedResponse<Customer> response = customerService.searchCustomers(emptyCriteria);
        List<Customer> customers = response.data();

        if (customers == null || customers.isEmpty()) {
            log.warn("No customers found to attach sample appointments.");
            return;
        }

        for (Customer customer : customers) {
            List<VehicleResponseDto> vehicles = vehicleAppService.getVehiclesByCustomerUuid(customer.getCustomerUuid());
            generateInactiveAppointments(customer, vehicles);
            generateActiveAppointments(customer, vehicles);
        }

        log.info("Appointment generation complete.");
    }

    private void generateInactiveAppointments(Customer customer, List<VehicleResponseDto> vehicles) {
        UUID vehicleUuid = vehicles.isEmpty() ? null : vehicles.get(0).vehicleUuid();
        Long vehicleId = vehicles.isEmpty() ? null : vehicles.get(0).id();

        for (int i = 1; i <= 5; i++) {
            LocalDateTime startDate = LocalDateTime.now().minusDays(30L + i).withMinute(0).withSecond(0).withNano(0);
            Appointment pastAppointment = new Appointment();
            pastAppointment.setCustomerId(customer.getId());
            pastAppointment.setCustomerUuid(customer.getCustomerUuid());
            pastAppointment.setVehicleId(vehicleId);
            pastAppointment.setVehicleUuid(vehicleUuid);
            pastAppointment.setStartDate(startDate);
            pastAppointment.setEndDate(startDate.plusMinutes(30));
            pastAppointment.setServiceIds(Collections.singletonList(2L)); // sample service ID (30 min)
            pastAppointment.setStatus(AppointmentStatus.COMPLETED);
            appointmentRepository.save(pastAppointment);
        }
    }

    private void generateActiveAppointments(Customer customer, List<VehicleResponseDto> vehicles) {
        UUID vehicleUuid = getPrimaryOrSecondaryVehicleUuid(vehicles);

        for (int i = 1; i <= 2; i++) {
            LocalDateTime startDate = LocalDateTime.now().plusDays(5L + i).withMinute(30).withSecond(0).withNano(0);
            
            List<Long> services = new ArrayList<>();
            services.add(1L); // Tire Change (30m)
            services.add(11L); // Balancing (30m)

            AppointmentRequestDto request = new AppointmentRequestDto(
                customer.getCustomerUuid(),
                vehicleUuid,
                startDate,
                startDate.plusMinutes(60), // Sum = 60m
                services,
                null
            );

            appointmentAppService.createAppointment(request);
        }
    }

    private UUID getPrimaryOrSecondaryVehicleUuid(List<VehicleResponseDto> vehicles) {
        if (vehicles.size() > 1) {
            return vehicles.get(1).vehicleUuid();
        }
        if (!vehicles.isEmpty()) {
            return vehicles.get(0).vehicleUuid();
        }
        return null;
    }
}
