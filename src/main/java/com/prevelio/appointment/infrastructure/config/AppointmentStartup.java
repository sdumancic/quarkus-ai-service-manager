package com.prevelio.appointment.infrastructure.config;

import java.time.LocalDateTime;
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

    public AppointmentStartup(AppointmentAppService appointmentAppService, CustomerService customerService,
            AppointmentRepository appointmentRepository) {
        this.appointmentAppService = appointmentAppService;
        this.customerService = customerService;
        this.appointmentRepository = appointmentRepository;
    }

    // Set priority to run after CustomerStartup (which runs at default priority of
    // 2500)
    void onStart(@Observes @Priority(Interceptor.Priority.APPLICATION + 1000) StartupEvent ev) {
        log.info("Generating sample appointments for customers...");

        // Fetch all customers using an empty criteria
        CustomerSearchCriteria emptyCriteria = new CustomerSearchCriteria();
        emptyCriteria.setPageIndex(0);
        emptyCriteria.setPageSize(100);
        PagedResponse<Customer> response = customerService.searchCustomers(emptyCriteria);
        List<Customer> customers = response.getData();

        if (customers == null || customers.isEmpty()) {
            log.warn("No customers found to attach sample appointments.");
            return;
        }

        for (Customer customer : customers) {

            generateInactiveAppointments(customer);
            generateActiveAppointments(customer);
        }

        log.info("Appointment generation complete.");
    }

    private void generateInactiveAppointments(Customer customer) {
        UUID vehicleUuid = customer.getVehicles().isEmpty() ? null : customer.getVehicles().get(0).getVehicleUuid();
        Long vehicleId = customer.getVehicles().isEmpty() ? null : customer.getVehicles().get(0).getId();

        for (int i = 1; i <= 5; i++) {
            Appointment pastAppointment = new Appointment();
            pastAppointment.setCustomerId(customer.getId());
            pastAppointment.setCustomerUuid(customer.getCustomerUuid());
            pastAppointment.setVehicleId(vehicleId);
            pastAppointment.setVehicleUuid(vehicleUuid);
            pastAppointment.setStartDate(LocalDateTime.now().minusDays(30L + i));
            pastAppointment.setEndDate(LocalDateTime.now().minusDays(30L + i).plusHours(2));
            pastAppointment.setServiceIds(Collections.singletonList(2L)); // sample service ID
            pastAppointment.setStatus(AppointmentStatus.COMPLETED);
            appointmentRepository.save(pastAppointment);
        }
    }

    private void generateActiveAppointments(Customer customer) {
        UUID vehicleUuid = getPrimaryOrSecondaryVehicleUuid(customer);

        for (int i = 1; i <= 2; i++) {
            AppointmentRequestDto request = new AppointmentRequestDto();
            request.setCustomerUuid(customer.getCustomerUuid());
            request.setVehicleUuid(vehicleUuid);
            request.setStartDate(LocalDateTime.now().plusDays(5L + i));
            request.setEndDate(LocalDateTime.now().plusDays(5L + i).plusHours(2));
            request.setServiceIds(Collections.singletonList(1L)); // sample service ID
            appointmentAppService.createAppointment(request);
        }
    }

    private UUID getPrimaryOrSecondaryVehicleUuid(Customer customer) {
        if (customer.getVehicles().size() > 1) {
            return customer.getVehicles().get(1).getVehicleUuid();
        }
        if (!customer.getVehicles().isEmpty()) {
            return customer.getVehicles().get(0).getVehicleUuid();
        }
        return null;
    }
}
