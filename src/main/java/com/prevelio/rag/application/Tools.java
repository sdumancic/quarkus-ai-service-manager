package com.prevelio.rag.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.prevelio.appointment.application.dto.AppointmentRequestDto;
import com.prevelio.appointment.application.dto.AppointmentResponseDto;
import com.prevelio.appointment.application.service.AppointmentAppService;
import com.prevelio.customer.application.service.CustomerService;
import com.prevelio.customer.domain.model.Customer;
import com.prevelio.service.application.dto.ServiceItemResponseDto;
import com.prevelio.service.application.service.ServiceItemAppService;
import com.prevelio.vehicle.application.dto.VehicleRequestDto;
import com.prevelio.vehicle.application.dto.VehicleResponseDto;
import com.prevelio.vehicle.application.service.VehicleAppService;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Tools {

    private final AppointmentAppService appointmentAppService;
    private final CustomerService customerAppService;
    private final ServiceItemAppService serviceAppService;
    private final VehicleAppService vehicleAppService;

    public Tools(AppointmentAppService appointmentAppService, CustomerService customerAppService,
            ServiceItemAppService serviceAppService, VehicleAppService vehicleAppService) {
        this.appointmentAppService = appointmentAppService;
        this.customerAppService = customerAppService;
        this.serviceAppService = serviceAppService;
        this.vehicleAppService = vehicleAppService;
    }

    @Tool
    public String getCurrentTime() {
        log.info("[TOOL] Getting current time");
        return java.time.LocalDateTime.now().toString();
    }

    @Tool
    public Customer getCustomerInfo(@ToolMemoryId UUID customerUuid) {
        log.info("[TOOL] Getting customer info for customerUuid: {}", customerUuid);
        var customer = customerAppService.getCustomerByUuid(customerUuid);
        log.info("[TOOL] Customer info: {}", customer);
        return customer;
    }

    @Tool
    public List<VehicleResponseDto> getCustomerVehicles(@ToolMemoryId UUID customerUuid) {
        log.info("[TOOL] Getting customer vehicles for customerUuid: {}", customerUuid);
        return vehicleAppService.getVehiclesByCustomerUuid(customerUuid);
    }

    @Tool
    public VehicleResponseDto createNewVehicle(@ToolMemoryId UUID customerUuid, String make, String model, int year,
            String licensePlate, String vin, String color) {
        log.info("[TOOL] Creating new vehicle for customerUuid: {}", customerUuid);
        VehicleRequestDto request = new VehicleRequestDto(
            customerUuid,
            make,
            model,
            year,
            licensePlate,
            vin,
            color,
            true
        );
        return vehicleAppService.createVehicle(request);
    }

    @Tool
    public List<AppointmentResponseDto> getActiveAppointments(@ToolMemoryId UUID customerUuid) {
        log.info("[TOOL] Getting active appointments for customerUuid: {}", customerUuid);
        var customer = customerAppService.getCustomerByUuid(customerUuid);
        return appointmentAppService.getActiveAppointmentsByCustomerId(customer.getId());
    }

    @Tool
    public AppointmentResponseDto createNewAppointment(@ToolMemoryId UUID customerUuid, String vehicleUuid,
            List<Long> serviceIds,
            LocalDateTime startTime) {
        log.info("[TOOL] Creating new appointment for customerUuid: {}", customerUuid);
        AppointmentRequestDto request = new AppointmentRequestDto(
            customerUuid,
            UUID.fromString(vehicleUuid),
            startTime,
            null,
            serviceIds,
            null
        );
        return appointmentAppService.createAppointment(request);
    }

    @Tool
    public AppointmentResponseDto updateAppointment(Long id, @ToolMemoryId UUID customerUuid, String vehicleUuid,
            List<Long> serviceIds,
            LocalDateTime startTime, LocalDateTime endTime) {
        log.info("[TOOL] Updating appointment for customerUuid: {}", customerUuid);
        AppointmentRequestDto request = new AppointmentRequestDto(
            customerUuid,
            UUID.fromString(vehicleUuid),
            startTime,
            endTime,
            serviceIds,
            null
        );
        return appointmentAppService.updateAppointment(id, request);
    }

    @Tool
    public void disableVehicle(String vehicleUuid) {
        log.info("[TOOL] Disabling vehicle for vehicleUuid: {}", vehicleUuid);
        vehicleAppService.disableVehicle(UUID.fromString(vehicleUuid));
    }

    @Tool
    public VehicleResponseDto updateVehicle(String vehicleUuid, @ToolMemoryId UUID customerUuid, String make,
            String model,
            int year,
            String licensePlate, String vin, String color) {
        log.info("[TOOL] Updating vehicle for customerUuid: {}", customerUuid);
        VehicleRequestDto request = new VehicleRequestDto(
            customerUuid,
            make,
            model,
            year,
            licensePlate,
            vin,
            color,
            true
        );
        return vehicleAppService.updateVehicle(UUID.fromString(vehicleUuid), request);
    }

    @Tool
    public VehicleResponseDto getVehicleByUuid(String vehicleUuid) {
        log.info("[TOOL] Getting vehicle by uuid {}", vehicleUuid);
        return vehicleAppService.getVehicleByUuid(UUID.fromString(vehicleUuid));
    }

    @Tool
    public void cancelAppointment(Long id) {
        log.info("[TOOL] Canceling appointment for id: {}", id);
        appointmentAppService.cancelAppointment(id);
    }

    @Tool
    public List<ServiceItemResponseDto> getListOfServices() {
        log.info("[TOOL] Getting list of services");
        return serviceAppService.getAllServices();
    }

}
