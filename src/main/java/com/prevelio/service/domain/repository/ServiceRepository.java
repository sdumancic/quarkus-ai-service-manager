package com.prevelio.service.domain.repository;

import java.util.List;
import java.util.Optional;

import com.prevelio.service.domain.model.ServiceItem;

public interface ServiceRepository {
    ServiceItem save(ServiceItem serviceItem);
    ServiceItem update(ServiceItem serviceItem);
    Optional<ServiceItem> findById(Long id);
    Optional<ServiceItem> findByCode(String code);
    List<ServiceItem> findAll();
    void delete(Long id);
}
