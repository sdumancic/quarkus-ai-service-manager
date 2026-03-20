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
        model.setCode(dto.code());
        model.setDescription(dto.description());
        model.setPrice(dto.price());
        model.setDurationInMinutes(dto.durationInMinutes());
        return model;
    }

    public static ServiceItemResponseDto toDto(ServiceItem model) {
        return new ServiceItemResponseDto(
            model.getId(),
            model.getCode(),
            model.getDescription(),
            model.getPrice(),
            model.getDurationInMinutes()
        );
    }
}
