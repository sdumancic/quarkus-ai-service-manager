package com.prevelio.customer.infrastructure.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import com.prevelio.customer.application.repository.CustomerRepository;
import com.prevelio.customer.domain.model.Customer;
import com.prevelio.customer.application.dto.CustomerSearchCriteria;
import com.prevelio.common.dto.PagedResponse;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class InMemoryCustomerRepository implements CustomerRepository {

    private final Map<Long, Customer> storage = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @Override
    public Customer getCustomerById(Long id) {
        if (id == null) {
            return null;
        }
        if (storage.get(id) == null) {
            return null;
        }
        return storage.get(id);
    }

    @Override
    public Customer getCustomerByUuid(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        log.info("[InMemoryCustomerRepository] Getting customer by uuid {}", uuid);
        var result = storage.values().stream()
                .filter(c -> uuid.equals(c.getCustomerUuid()))
                .findFirst()
                .orElse(null);
        log.info("[InMemoryCustomerRepository] Customer found: {}", result);
        return result;
    }

    @Override
    public PagedResponse<Customer> searchCustomers(CustomerSearchCriteria criteria) {
        java.util.List<Customer> filtered = storage.values().stream()
                .filter(c -> criteria.name() == null || (c.getName() != null
                        && c.getName().toLowerCase().contains(criteria.name().toLowerCase())))
                .filter(c -> criteria.email() == null || (c.getEmail() != null
                        && c.getEmail().toLowerCase().contains(criteria.email().toLowerCase())))
                .toList();

        long totalItems = filtered.size();
        int pageIndex = criteria.getPageIndexOrDefault();
        int pageSize = criteria.getPageSizeOrDefault();

        int fromIndex = Math.min(pageIndex * pageSize, (int) totalItems);
        int toIndex = Math.min((pageIndex + 1) * pageSize, (int) totalItems);

        java.util.List<Customer> pagedData = filtered.subList(fromIndex, toIndex);

        return new com.prevelio.common.dto.PagedResponse<>(pagedData,
                new com.prevelio.common.dto.PageMetadata(pageSize, totalItems, pageIndex));
    }

    @Override
    public Customer createCustomer(Customer customer) {
        Long id = idGenerator.incrementAndGet();
        customer.setId(id);
        storage.put(id, customer);
        return customer;
    }

    @Override
    public Customer updateCustomer(Customer customer) {
        if (!storage.containsKey(customer.getId())) {
            return null;
        }
        storage.put(customer.getId(), customer);
        return customer;
    }

    @Override
    public void deleteCustomer(Customer customer) {
        storage.remove(customer.getId());
    }
}
