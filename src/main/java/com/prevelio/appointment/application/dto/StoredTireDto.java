package com.prevelio.appointment.application.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoredTireDto {
    private String brand;
    private String model;
    private Integer width;
    private Integer aspectRatio;
    private Integer diameter;
    private String season;
    private String condition;
}
