package com.prevelio.customer.application.mapper;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import com.prevelio.customer.application.dto.CustomerRequestDto;
import com.prevelio.customer.application.dto.CustomerResponseDto;
import com.prevelio.customer.domain.model.Customer;
import com.prevelio.customer.domain.model.CustomerStatus;

public class CustomerMapper {
    private CustomerMapper() {
        /* This utility class should not be instantiated */
    }

    public static Customer toCustomer(CustomerRequestDto customerRequestDto, Optional<Long> id) {

        Customer customer = new Customer();
        id.ifPresent(customer::setId);
        customer.setName(customerRequestDto.name());
        customer.setEmail(customerRequestDto.email());
        customer.setPhone(customerRequestDto.phone());
        customer.setAddress(customerRequestDto.address());
        customer.setCity(customerRequestDto.city());
        customer.setState(customerRequestDto.state());
        customer.setZip(customerRequestDto.zip());
        customer.setCountry(customerRequestDto.country());
        customer.setCustomerUuid(UUID.randomUUID());
        customer.setStatus(
                customerRequestDto.status() != null ? customerRequestDto.status() : CustomerStatus.ACTIVE);
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        return customer;
    }

    public static CustomerResponseDto toCustomerResponseDto(Customer customer) {
        return new CustomerResponseDto(
            customer.getId(),
            customer.getName(),
            customer.getEmail(),
            customer.getPhone(),
            customer.getAddress(),
            customer.getCity(),
            customer.getState(),
            customer.getZip(),
            customer.getCountry(),
            customer.getStatus(),
            customer.getCreatedAt(),
            customer.getUpdatedAt(),
            customer.getCustomerUuid()
        );
    }
}
