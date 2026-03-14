package com.prevelio.customer.infrastructure.config;

import com.prevelio.customer.application.dto.CustomerRequestDto;
import com.prevelio.customer.application.service.CustomerService;
import com.prevelio.customer.domain.model.Customer;
import com.prevelio.customer.domain.model.CustomerStatus;
import com.prevelio.customer.domain.model.Vehicle;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
@ApplicationScoped
public class CustomerStartup {

    private final CustomerService customerService;

    public CustomerStartup(CustomerService customerService) {
        this.customerService = customerService;
    }

    void onStart(@Observes StartupEvent ev) {
        log.info("Generating 10 initial customers with vehicles...");
        for (int i = 1; i <= 10; i++) {
            CustomerRequestDto requestDto = new CustomerRequestDto(
                    "Customer " + i,
                    "customer" + i + "@example.com",
                    "555-010" + i,
                    i + " Main St",
                    "City " + i,
                    "State",
                    "1000" + i,
                    "Country",
                    CustomerStatus.ACTIVE);
            Customer customer = customerService.createCustomer(requestDto);
            
            // Add vehicles
            addSampleVehicles(customer);
            
            log.info("Created customer: {} with 2 vehicles", customer.getName());
        }
        log.info("Customer generation complete.");
    }

    private void addSampleVehicles(Customer customer) {
        Vehicle v1 = new Vehicle();
        v1.setVehicleUuid(UUID.randomUUID());
        v1.setMake("Toyota");
        v1.setModel("Camry");
        v1.setYear(2020);
        v1.setLicensePlate("ABC-123" + customer.getId());
        customerService.addVehicle(customer.getId(), v1);

        Vehicle v2 = new Vehicle();
        v2.setVehicleUuid(UUID.randomUUID());
        v2.setMake("Honda");
        v2.setModel("Civic");
        v2.setYear(2022);
        v2.setLicensePlate("XYZ-789" + customer.getId());
        customerService.addVehicle(customer.getId(), v2);
    }
}
