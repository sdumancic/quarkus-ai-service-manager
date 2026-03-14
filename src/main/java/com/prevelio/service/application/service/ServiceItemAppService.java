package com.prevelio.service.application.service;

import java.util.List;

import com.prevelio.service.application.dto.ServiceItemRequestDto;
import com.prevelio.service.application.dto.ServiceItemResponseDto;
import com.prevelio.service.application.mapper.ServiceItemMapper;
import com.prevelio.service.domain.model.ServiceItem;
import com.prevelio.service.domain.repository.ServiceRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class ServiceItemAppService {

    private final ServiceRepository repository;

    @Inject
    public ServiceItemAppService(ServiceRepository repository) {
        this.repository = repository;
    }

    public List<ServiceItemResponseDto> getAllServices() {
        return repository.findAll().stream()
                .map(ServiceItemMapper::toDto)
                .toList();
    }

    public ServiceItemResponseDto getServiceById(Long id) {
        return repository.findById(id)
                .map(ServiceItemMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Service item not found"));
    }

    public ServiceItemResponseDto createService(ServiceItemRequestDto request) {
        ServiceItem domain = ServiceItemMapper.toDomain(request);
        ServiceItem saved = repository.save(domain);
        return ServiceItemMapper.toDto(saved);
    }

    public ServiceItemResponseDto updateService(Long id, ServiceItemRequestDto request) {
        ServiceItem existing = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Service item not found"));
        
        existing.setCode(request.getCode());
        existing.setDescription(request.getDescription());
        existing.setPrice(request.getPrice());
        existing.setDurationInMinutes(request.getDurationInMinutes());
        
        ServiceItem updated = repository.update(existing);
        return ServiceItemMapper.toDto(updated);
    }

    public void deleteService(Long id) {
        if (repository.findById(id).isEmpty()) {
            throw new NotFoundException("Service item not found");
        }
        repository.delete(id);
    }
}
