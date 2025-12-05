package com.bfb.interfaces.rest.config;

import com.bfb.business.contract.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

import static com.bfb.interfaces.rest.config.ApiConstants.ErrorMessages;
import static com.bfb.interfaces.rest.config.ApiConstants.ErrorTitles;
import static com.bfb.interfaces.rest.config.ApiConstants.ErrorTypes;

/**
 * Global exception handler for all REST controllers.
 * Maps business exceptions to appropriate HTTP codes using RFC 7807 Problem Details.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({
        ContractNotFoundException.class,
        com.bfb.business.vehicle.exception.VehicleNotFoundException.class,
        com.bfb.business.client.exception.ClientNotFoundException.class
    })
    public ProblemDetail handleNotFound(RuntimeException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, 
            ex.getMessage()
        );
        problemDetail.setTitle(ErrorTitles.NOT_FOUND);
        problemDetail.setType(URI.create(ErrorTypes.NOT_FOUND));
        return problemDetail;
    }

    @ExceptionHandler({ValidationException.class})
    public ProblemDetail handleValidation(ValidationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, 
            ex.getMessage()
        );
        problemDetail.setTitle(ErrorTitles.VALIDATION_FAILED);
        problemDetail.setType(URI.create(ErrorTypes.VALIDATION));
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(err -> err.getField() + ": " + err.getDefaultMessage())
            .reduce("", (a, b) -> a + "; " + b);
            
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, 
            "Validation errors: " + errors
        );
        problemDetail.setTitle(ErrorTitles.PARAMETER_VALIDATION_FAILED);
        problemDetail.setType(URI.create(ErrorTypes.VALIDATION));
        return problemDetail;
    }

    @ExceptionHandler({
        OverlapException.class, 
        VehicleUnavailableException.class, 
        ClientUnknownException.class,
        com.bfb.business.client.exception.DuplicateEmailException.class,
        com.bfb.business.client.exception.DuplicateClientException.class,
        com.bfb.business.client.exception.DuplicateLicenseException.class,
        com.bfb.business.vehicle.exception.DuplicateVehicleException.class
    })
    public ProblemDetail handleConflict(RuntimeException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT, 
            ex.getMessage()
        );
        problemDetail.setTitle(ErrorTitles.BUSINESS_CONFLICT);
        problemDetail.setType(URI.create(ErrorTypes.CONFLICT));
        return problemDetail;
    }

    @ExceptionHandler(TransitionNotAllowedException.class)
    public ProblemDetail handleTransitionNotAllowed(TransitionNotAllowedException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNPROCESSABLE_ENTITY, 
            ex.getMessage()
        );
        problemDetail.setTitle(ErrorTitles.TRANSITION_NOT_ALLOWED);
        problemDetail.setType(URI.create(ErrorTypes.TRANSITION_NOT_ALLOWED));
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, 
            ErrorMessages.INTERNAL_ERROR
        );
        problemDetail.setTitle(ErrorTitles.INTERNAL_ERROR);
        problemDetail.setType(URI.create(ErrorTypes.INTERNAL_ERROR));
        return problemDetail;
    }
}
