package com.prevelio.common.exception;

import com.prevelio.common.dto.ErrorResponseDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Provider
@Slf4j
public class GlobalExceptionHandler implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        log.error("Unhandled exception: ", exception);

        if (exception instanceof ConstraintViolationException cvException) {
            return handleConstraintViolation(cvException);
        }

        if (exception instanceof NotFoundException nfException) {
            return createResponse(nfException.getMessage(), Response.Status.NOT_FOUND);
        }

        if (exception instanceof BadRequestException brException) {
            return createResponse(brException.getMessage(), Response.Status.BAD_REQUEST);
        }

        if (exception instanceof IllegalStateException isException) {
            return createResponse(isException.getMessage(), Response.Status.BAD_REQUEST);
        }

        if (exception instanceof WebApplicationException waException) {
            return createResponse(waException.getMessage(), Response.Status.fromStatusCode(waException.getResponse().getStatus()));
        }

        return createResponse("An unexpected error occurred: " + exception.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
    }

    private Response handleConstraintViolation(ConstraintViolationException exception) {
        List<String> details = exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .toList();

        ErrorResponseDto error = new ErrorResponseDto("Validation failed", Response.Status.BAD_REQUEST.getStatusCode());
        error.setDetails(details);
        return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
    }

    private Response createResponse(String message, Response.Status status) {
        ErrorResponseDto error = new ErrorResponseDto(message, status.getStatusCode());
        return Response.status(status).entity(error).build();
    }
}
