package com.prevelio.appointment.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StoredTire {
    private String brand;
    private String model;
    private Integer width;
    private Integer aspectRatio;
    private Integer diameter;
    private String season;
    private String condition;
}
