package com.prevelio.service.application.mapper;

import com.prevelio.service.application.dto.ServiceItemRequestDto;
import com.prevelio.service.application.dto.ServiceItemResponseDto;
import com.prevelio.service.domain.model.ServiceItem;

public class ServiceItemMapper {
    private ServiceItemMapper() {
        /* Utility class */
    }

    public static ServiceItem toDomain(ServiceItemRequestDto dto) {
        ServiceItem model = new ServiceItem();
        model.setCode(dto.getCode());
        model.setDescription(dto.getDescription());
        model.setPrice(dto.getPrice());
        model.setDurationInMinutes(dto.getDurationInMinutes());
        return model;
    }

    public static ServiceItemResponseDto toDto(ServiceItem model) {
        ServiceItemResponseDto dto = new ServiceItemResponseDto();
        dto.setId(model.getId());
        dto.setCode(model.getCode());
        dto.setDescription(model.getDescription());
        dto.setPrice(model.getPrice());
        dto.setDurationInMinutes(model.getDurationInMinutes());
        return dto;
    }
}
