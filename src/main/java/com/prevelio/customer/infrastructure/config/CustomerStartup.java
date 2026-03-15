package com.prevelio.customer.infrastructure.config;

import com.prevelio.customer.application.dto.CustomerRequestDto;
import com.prevelio.customer.application.service.CustomerService;
import com.prevelio.customer.domain.model.Customer;
import com.prevelio.customer.domain.model.CustomerStatus;
import com.prevelio.vehicle.application.dto.VehicleRequestDto;
import com.prevelio.vehicle.application.service.VehicleAppService;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class CustomerStartup {

    private final CustomerService customerService;
    private final VehicleAppService vehicleAppService;

    public CustomerStartup(CustomerService customerService, VehicleAppService vehicleAppService) {
        this.customerService = customerService;
        this.vehicleAppService = vehicleAppService;
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
        VehicleRequestDto v1 = new VehicleRequestDto();
        v1.setCustomerUuid(customer.getCustomerUuid());
        v1.setMake("Toyota");
        v1.setModel("Camry");
        v1.setYear(2020);
        v1.setLicensePlate("ABC-123" + customer.getId());
        vehicleAppService.createVehicle(v1);

        VehicleRequestDto v2 = new VehicleRequestDto();
        v2.setCustomerUuid(customer.getCustomerUuid());
        v2.setMake("Honda");
        v2.setModel("Civic");
        v2.setYear(2022);
        v2.setLicensePlate("XYZ-789" + customer.getId());
        vehicleAppService.createVehicle(v2);
    }
}
