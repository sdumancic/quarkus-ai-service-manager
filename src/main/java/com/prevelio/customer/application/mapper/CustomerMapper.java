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
        customer.setName(customerRequestDto.getName());
        customer.setEmail(customerRequestDto.getEmail());
        customer.setPhone(customerRequestDto.getPhone());
        customer.setAddress(customerRequestDto.getAddress());
        customer.setCity(customerRequestDto.getCity());
        customer.setState(customerRequestDto.getState());
        customer.setZip(customerRequestDto.getZip());
        customer.setCountry(customerRequestDto.getCountry());
        customer.setCustomerUuid(UUID.randomUUID());
        customer.setStatus(
                customerRequestDto.getStatus() != null ? customerRequestDto.getStatus() : CustomerStatus.ACTIVE);
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        return customer;
    }

    public static CustomerResponseDto toCustomerResponseDto(Customer customer) {
        CustomerResponseDto customerResponseDto = new CustomerResponseDto();
        customerResponseDto.setId(customer.getId());
        customerResponseDto.setName(customer.getName());
        customerResponseDto.setEmail(customer.getEmail());
        customerResponseDto.setPhone(customer.getPhone());
        customerResponseDto.setAddress(customer.getAddress());
        customerResponseDto.setCity(customer.getCity());
        customerResponseDto.setState(customer.getState());
        customerResponseDto.setZip(customer.getZip());
        customerResponseDto.setCountry(customer.getCountry());
        customerResponseDto.setStatus(customer.getStatus());
        customerResponseDto.setCreatedAt(customer.getCreatedAt());
        customerResponseDto.setUpdatedAt(customer.getUpdatedAt());
        customerResponseDto.setCustomerUuid(customer.getCustomerUuid());
        return customerResponseDto;
    }
}
