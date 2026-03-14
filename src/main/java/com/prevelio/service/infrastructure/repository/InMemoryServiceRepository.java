package com.prevelio.service.infrastructure.repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.prevelio.service.domain.model.ServiceItem;
import com.prevelio.service.domain.repository.ServiceRepository;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InMemoryServiceRepository implements ServiceRepository {

    private final Map<Long, ServiceItem> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @PostConstruct
    void init() {
        createPredefined("TIRE_CHANGE_16", "Tire Change 16\"", new BigDecimal("40.00"), 30);
        createPredefined("TIRE_CHANGE_17", "Tire Change 17\"", new BigDecimal("50.00"), 30);
        createPredefined("TIRE_CHANGE_18", "Tire Change 18\"", new BigDecimal("60.00"), 30);
        createPredefined("TIRE_CHANGE_19", "Tire Change 19\"", new BigDecimal("70.00"), 30);
        createPredefined("TIRE_CHANGE_20", "Tire Change 20\"", new BigDecimal("80.00"), 30);
        
        createPredefined("TIRE_STORAGE_16", "Tire Storage 16\"", new BigDecimal("30.00"), 0);
        createPredefined("TIRE_STORAGE_17", "Tire Storage 17\"", new BigDecimal("35.00"), 0);
        createPredefined("TIRE_STORAGE_18", "Tire Storage 18\"", new BigDecimal("40.00"), 0);
        createPredefined("TIRE_STORAGE_19", "Tire Storage 19\"", new BigDecimal("45.00"), 0);
        createPredefined("TIRE_STORAGE_20", "Tire Storage 20\"", new BigDecimal("50.00"), 0);

        createPredefined("BALANCING_16", "Balancing Tires 16\"", new BigDecimal("20.00"), 30);
        createPredefined("BALANCING_17", "Balancing Tires 17\"", new BigDecimal("25.00"), 30);
        createPredefined("BALANCING_18", "Balancing Tires 18\"", new BigDecimal("30.00"), 30);
        createPredefined("BALANCING_19", "Balancing Tires 19\"", new BigDecimal("35.00"), 30);
        createPredefined("BALANCING_20", "Balancing Tires 20\"", new BigDecimal("40.00"), 30);
    }

    private void createPredefined(String code, String desc, BigDecimal price, Integer duration) {
        Long id = idGenerator.incrementAndGet();
        ServiceItem item = new ServiceItem(id, code, desc, price, duration);
        storage.put(item.getId(), item);
    }

    @Override
    public ServiceItem save(ServiceItem serviceItem) {
        Long id = idGenerator.incrementAndGet();
        serviceItem.setId(id);
        storage.put(id, serviceItem);
        return serviceItem;
    }

    @Override
    public ServiceItem update(ServiceItem serviceItem) {
        storage.put(serviceItem.getId(), serviceItem);
        return serviceItem;
    }

    @Override
    public Optional<ServiceItem> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<ServiceItem> findByCode(String code) {
        return storage.values().stream()
                .filter(s -> code.equals(s.getCode()))
                .findFirst();
    }

    @Override
    public List<ServiceItem> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void delete(Long id) {
        storage.remove(id);
    }
}
