package com.prevelio.vehicle.domain.model;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Vehicle {
    private Long id;
    private UUID vehicleUuid;
    private Long customerId;
    private UUID customerUuid;
    private String make;
    private String model;
    private Integer year;
    private String licensePlate;
    private String vin;
    private String color;
    private boolean active = true;
}
