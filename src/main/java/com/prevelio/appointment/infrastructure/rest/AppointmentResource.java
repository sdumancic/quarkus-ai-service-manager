package com.prevelio.appointment.infrastructure.rest;

import java.util.List;

import com.prevelio.appointment.application.dto.AppointmentRequestDto;
import com.prevelio.appointment.application.dto.AppointmentResponseDto;
import com.prevelio.appointment.application.service.AppointmentAppService;
import com.prevelio.customer.application.service.CustomerService;

import jakarta.enterprise.context.RequestScoped;
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

@Path("/appointments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class AppointmentResource {

    private final AppointmentAppService appointmentAppService;
    private final CustomerService customerService;

    @Inject
    public AppointmentResource(AppointmentAppService appointmentAppService, CustomerService customerService) {
        this.appointmentAppService = appointmentAppService;
        this.customerService = customerService;
    }

    @GET
    public Response getAllAppointments() {
        List<AppointmentResponseDto> appointments = appointmentAppService.getAllAppointments();
        return Response.ok(appointments).build();
    }

    @GET
    @Path("/{id}")
    public Response getAppointmentById(@PathParam("id") Long id) {
        AppointmentResponseDto appointment = appointmentAppService.getAppointmentById(id);
        return Response.ok(appointment).build();
    }

    @GET
    @Path("/customer/{uuid}/active")
    public Response getActiveAppointmentsByCustomerUuid(@PathParam("uuid") java.util.UUID customerUuid) {
        // Need to get customer by UUID to get its ID, but this is Appointment resource...
        // We probably should resolve Customer inside a broader service or inject CustomerService here.
        // Let's inject CustomerService.
        Long customerId = customerService.getCustomerByUuid(customerUuid).getId();
        List<AppointmentResponseDto> appointments = appointmentAppService.getActiveAppointmentsByCustomerId(customerId);
        return Response.ok(appointments).build();
    }

    @POST
    public Response createAppointment(@Valid AppointmentRequestDto request) {
        AppointmentResponseDto created = appointmentAppService.createAppointment(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateAppointment(@PathParam("id") Long id, @Valid AppointmentRequestDto request) {
        AppointmentResponseDto updated = appointmentAppService.updateAppointment(id, request);
        return Response.ok(updated).build();
    }

    @PUT
    @Path("/{id}/cancel")
    public Response cancelAppointment(@PathParam("id") Long id) {
        appointmentAppService.cancelAppointment(id);
        return Response.ok().build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteAppointment(@PathParam("id") Long id) {
        appointmentAppService.deleteAppointment(id);
        return Response.noContent().build();
    }
}
