package com.prevelio.vehicle.infrastructure.rest;

import java.util.List;
import java.util.UUID;

import com.prevelio.vehicle.application.dto.VehicleRequestDto;
import com.prevelio.vehicle.application.dto.VehicleResponseDto;
import com.prevelio.vehicle.application.service.VehicleAppService;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/vehicles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VehicleResource {

    private final VehicleAppService vehicleAppService;

    @Inject
    public VehicleResource(VehicleAppService vehicleAppService) {
        this.vehicleAppService = vehicleAppService;
    }

    @GET
    @Path("/customer/{customerUuid}")
    public List<VehicleResponseDto> getCustomerVehicles(@PathParam("customerUuid") UUID customerUuid) {
        return vehicleAppService.getVehiclesByCustomerUuid(customerUuid);
    }

    @GET
    @Path("/{vehicleUuid}")
    public VehicleResponseDto getVehicle(@PathParam("vehicleUuid") UUID vehicleUuid) {
        return vehicleAppService.getVehicleByUuid(vehicleUuid);
    }

    @POST
    public Response createVehicle(@Valid VehicleRequestDto request) {
        VehicleResponseDto created = vehicleAppService.createVehicle(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{vehicleUuid}")
    public VehicleResponseDto updateVehicle(@PathParam("vehicleUuid") UUID vehicleUuid, @Valid VehicleRequestDto request) {
        return vehicleAppService.updateVehicle(vehicleUuid, request);
    }

    @DELETE
    @Path("/{vehicleUuid}")
    public Response disableVehicle(@PathParam("vehicleUuid") UUID vehicleUuid) {
        vehicleAppService.disableVehicle(vehicleUuid);
        return Response.noContent().build();
    }
}
