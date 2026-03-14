package com.prevelio.customer.application.service;

import java.util.Optional;
import java.util.UUID;

import com.prevelio.common.dto.PagedResponse;
import com.prevelio.customer.application.dto.CustomerRequestDto;
import com.prevelio.customer.application.dto.CustomerSearchCriteria;
import com.prevelio.customer.application.mapper.CustomerMapper;
import com.prevelio.customer.application.repository.CustomerRepository;
import com.prevelio.customer.domain.model.Customer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer getCustomerById(Long id) {
        Customer customer = customerRepository.getCustomerById(id);
        if (customer == null) {
            throw new NotFoundException("Customer not found");
        }
        return customer;
    }

    public Customer getCustomerByUuid(UUID uuid) {
        Customer customer = customerRepository.getCustomerByUuid(uuid);
        if (customer == null) {
            throw new NotFoundException("Customer not found");
        }
        return customer;
    }

    public PagedResponse<Customer> searchCustomers(CustomerSearchCriteria criteria) {
        return customerRepository.searchCustomers(criteria);
    }

    public Customer createCustomer(CustomerRequestDto customerRequestDto) {
        Customer customer = CustomerMapper.toCustomer(customerRequestDto, Optional.empty());
        return customerRepository.createCustomer(customer);
    }

    public Customer updateCustomer(CustomerRequestDto customerRequestDto, Long id) {
        Customer customer = CustomerMapper.toCustomer(customerRequestDto, Optional.of(id));
        return customerRepository.updateCustomer(customer);
    }

    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.getCustomerById(id);
        if (customer != null) {
            customerRepository.deleteCustomer(customer);
        }
    }

}
