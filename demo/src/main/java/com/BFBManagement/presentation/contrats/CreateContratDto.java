package com.BFBManagement.presentation.contrats;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;
import java.lang.annotation.*;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO pour la création d'un contrat.
 */
@ValidDateRange
public record CreateContratDto(
    @NotNull(message = "clientId est obligatoire")
    UUID clientId,
    
    @NotNull(message = "vehiculeId est obligatoire")
    UUID vehiculeId,
    
    @NotNull(message = "dateDebut est obligatoire")
    LocalDate dateDebut,
    
    @NotNull(message = "dateFin est obligatoire")
    LocalDate dateFin
) {}

/**
 * Annotation de validation pour vérifier que dateFin > dateDebut.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateRangeValidator.class)
@Documented
@interface ValidDateRange {
    String message() default "La dateFin doit être postérieure à la dateDebut";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

/**
 * Validateur pour l'annotation ValidDateRange.
 */
class DateRangeValidator implements ConstraintValidator<ValidDateRange, CreateContratDto> {
    @Override
    public boolean isValid(CreateContratDto dto, ConstraintValidatorContext context) {
        if (dto == null || dto.dateDebut() == null || dto.dateFin() == null) {
            return true; // Les @NotNull gèrent déjà ces cas
        }
        return dto.dateFin().isAfter(dto.dateDebut());
    }
}
