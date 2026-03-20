package com.prevelio.appointment.application.dto;

public record StoredTireDto(
    String brand,
    String model,
    Integer width,
    Integer aspectRatio,
    Integer diameter,
    String season,
    String condition
) {}
