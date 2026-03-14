package com.prevelio.service.infrastructure.rest;

import java.util.List;

import com.prevelio.service.application.dto.ServiceItemRequestDto;
import com.prevelio.service.application.dto.ServiceItemResponseDto;
import com.prevelio.service.application.service.ServiceItemAppService;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
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

@Path("/services")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class ServiceResource {

    private final ServiceItemAppService serviceItemAppService;

    @Inject
    public ServiceResource(ServiceItemAppService serviceItemAppService) {
        this.serviceItemAppService = serviceItemAppService;
    }

    @GET
    public Response getAllServices() {
        List<ServiceItemResponseDto> services = serviceItemAppService.getAllServices();
        return Response.ok(services).build();
    }

    @GET
    @Path("/{id}")
    public Response getServiceById(@PathParam("id") Long id) {
        ServiceItemResponseDto service = serviceItemAppService.getServiceById(id);
        return Response.ok(service).build();
    }

    @POST
    public Response createService(ServiceItemRequestDto request) {
        ServiceItemResponseDto created = serviceItemAppService.createService(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateService(@PathParam("id") Long id, ServiceItemRequestDto request) {
        ServiceItemResponseDto updated = serviceItemAppService.updateService(id, request);
        return Response.ok(updated).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteService(@PathParam("id") Long id) {
        serviceItemAppService.deleteService(id);
        return Response.noContent().build();
    }
}
