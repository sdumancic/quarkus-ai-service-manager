package com.prevelio.customer.infrastructure.rest;

import java.util.UUID;
import java.util.List;

import com.prevelio.common.dto.PagedResponse;
import com.prevelio.customer.application.dto.CustomerRequestDto;
import com.prevelio.customer.application.dto.CustomerResponseDto;
import com.prevelio.customer.application.dto.CustomerSearchCriteria;
import com.prevelio.customer.application.mapper.CustomerMapper;
import com.prevelio.customer.application.service.CustomerService;
import com.prevelio.customer.domain.model.Customer;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@RequestScoped
@Path("/customers")
public class CustomerResource {

    private final CustomerService customerService;

    public CustomerResource(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GET
    @Path("/{id}")
    public CustomerResponseDto getCustomerById(@PathParam("id") Long id) {
        Customer customer = customerService.getCustomerById(id);
        return CustomerMapper.toCustomerResponseDto(customer);
    }

    @GET
    @Path("/uuid/{uuid}")
    public CustomerResponseDto getCustomerByUuid(@PathParam("uuid") UUID uuid) {
        Customer customer = customerService.getCustomerByUuid(uuid);
        return CustomerMapper.toCustomerResponseDto(customer);
    }

    @GET
    public PagedResponse<CustomerResponseDto> searchCustomers(@BeanParam CustomerSearchCriteria criteria) {
        PagedResponse<Customer> pagedCustomers = customerService.searchCustomers(criteria);

        List<CustomerResponseDto> dtoList = pagedCustomers.getData().stream()
                .map(CustomerMapper::toCustomerResponseDto)
                .toList();

        return new PagedResponse<>(dtoList, pagedCustomers.getMetadata());
    }

    @POST
    public CustomerResponseDto createCustomer(CustomerRequestDto customerRequestDto) {
        Customer customer = customerService.createCustomer(customerRequestDto);
        return CustomerMapper.toCustomerResponseDto(customer);
    }

    @PUT
    @Path("/{id}")
    public CustomerResponseDto updateCustomer(CustomerRequestDto customerRequestDto, @PathParam("id") Long id) {
        Customer customer = customerService.updateCustomer(customerRequestDto, id);
        return CustomerMapper.toCustomerResponseDto(customer);
    }

    @DELETE
    @Path("/{id}")
    public void deleteCustomer(@PathParam("id") Long id) {
        customerService.deleteCustomer(id);
    }
}
