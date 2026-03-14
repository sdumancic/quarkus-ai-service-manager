package com.prevelio.customer.application.repository;

import java.util.UUID;

import com.prevelio.common.dto.PagedResponse;
import com.prevelio.customer.application.dto.CustomerSearchCriteria;
import com.prevelio.customer.domain.model.Customer;

public interface CustomerRepository {
    Customer getCustomerById(Long id);

    Customer getCustomerByUuid(UUID uuid);

    PagedResponse<Customer> searchCustomers(CustomerSearchCriteria criteria);

    Customer createCustomer(Customer customer);

    Customer updateCustomer(Customer customer);

    void deleteCustomer(Customer customer);
}
