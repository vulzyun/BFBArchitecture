package com.bfb.interfaces.rest.config;

import com.bfb.business.contract.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

/**
 * Global exception handler for all REST controllers.
 * Maps business exceptions to appropriate HTTP codes.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ContractNotFoundException.class)
    public ProblemDetail handleContractNotFound(ContractNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, 
            ex.getMessage()
        );
        problemDetail.setTitle("Contract not found");
        problemDetail.setType(URI.create("https://bfbmanagement.com/errors/contract-not-found"));
        return problemDetail;
    }

    @ExceptionHandler({ValidationException.class})
    public ProblemDetail handleValidation(ValidationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, 
            ex.getMessage()
        );
        problemDetail.setTitle("Validation failed");
        problemDetail.setType(URI.create("https://bfbmanagement.com/errors/validation"));
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
        problemDetail.setTitle("Parameter validation failed");
        problemDetail.setType(URI.create("https://bfbmanagement.com/errors/validation"));
        return problemDetail;
    }

    @ExceptionHandler({OverlapException.class, VehicleUnavailableException.class, ClientUnknownException.class})
    public ProblemDetail handleConflict(RuntimeException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT, 
            ex.getMessage()
        );
        problemDetail.setTitle("Business conflict");
        problemDetail.setType(URI.create("https://bfbmanagement.com/errors/conflict"));
        return problemDetail;
    }

    @ExceptionHandler(TransitionNotAllowedException.class)
    public ProblemDetail handleTransitionNotAllowed(TransitionNotAllowedException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNPROCESSABLE_ENTITY, 
            ex.getMessage()
        );
        problemDetail.setTitle("State transition not allowed");
        problemDetail.setType(URI.create("https://bfbmanagement.com/errors/transition-not-allowed"));
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, 
            "An internal error occurred"
        );
        problemDetail.setTitle("Internal error");
        problemDetail.setType(URI.create("https://bfbmanagement.com/errors/internal"));
        return problemDetail;
    }
}
