package com.BFBManagement.presentation.contrats;

import com.BFBManagement.business.contrats.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

/**
 * Gestionnaire global des exceptions pour le domaine Contrats.
 * Mappe les exceptions métier vers les codes HTTP appropriés.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ContratNotFoundException.class)
    public ProblemDetail handleContratNotFound(ContratNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, 
            ex.getMessage()
        );
        problemDetail.setTitle("Contrat introuvable");
        problemDetail.setType(URI.create("https://bfbmanagement.com/errors/contrat-not-found"));
        return problemDetail;
    }

    @ExceptionHandler({ValidationException.class})
    public ProblemDetail handleValidation(ValidationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, 
            ex.getMessage()
        );
        problemDetail.setTitle("Validation échouée");
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
            "Erreurs de validation: " + errors
        );
        problemDetail.setTitle("Validation des paramètres échouée");
        problemDetail.setType(URI.create("https://bfbmanagement.com/errors/validation"));
        return problemDetail;
    }

    @ExceptionHandler({OverlapException.class, VehicleUnavailableException.class, ClientUnknownException.class})
    public ProblemDetail handleConflict(RuntimeException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT, 
            ex.getMessage()
        );
        problemDetail.setTitle("Conflit métier");
        problemDetail.setType(URI.create("https://bfbmanagement.com/errors/conflict"));
        return problemDetail;
    }

    @ExceptionHandler(TransitionNotAllowedException.class)
    public ProblemDetail handleTransitionNotAllowed(TransitionNotAllowedException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNPROCESSABLE_ENTITY, 
            ex.getMessage()
        );
        problemDetail.setTitle("Transition d'état interdite");
        problemDetail.setType(URI.create("https://bfbmanagement.com/errors/transition-not-allowed"));
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, 
            "Une erreur interne s'est produite"
        );
        problemDetail.setTitle("Erreur interne");
        problemDetail.setType(URI.create("https://bfbmanagement.com/errors/internal"));
        return problemDetail;
    }
}
