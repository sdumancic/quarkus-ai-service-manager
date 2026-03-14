package com.prevelio.customer.infrastructure.config;

import com.prevelio.customer.application.dto.CustomerRequestDto;
import com.prevelio.customer.application.service.CustomerService;
import com.prevelio.customer.domain.model.CustomerStatus;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class CustomerStartup {

    private final CustomerService customerService;

    public CustomerStartup(CustomerService customerService) {
        this.customerService = customerService;
    }

    void onStart(@Observes StartupEvent ev) {
        log.info("Generating 10 initial customers...");
        for (int i = 1; i <= 10; i++) {
            CustomerRequestDto customer = new CustomerRequestDto(
                    "Customer " + i,
                    "customer" + i + "@example.com",
                    "555-010" + i,
                    i + " Main St",
                    "City " + i,
                    "State",
                    "1000" + i,
                    "Country",
                    CustomerStatus.ACTIVE);
            customerService.createCustomer(customer);
            log.info("Created customer: {}", customer.getName());
        }
        log.info("Customer generation complete.");
    }
}
