package com.BFBManagement.adapters.in.listeners.contrats;

import com.BFBManagement.application.contrats.ports.in.ContratUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Listener REST interne pour gérer les événements liés aux véhicules.
 * Ces endpoints sont destinés à être appelés par d'autres services internes.
 */
@RestController
@RequestMapping("/internal/events/vehicules")
@Tag(name = "Vehicle Events (Internal)", description = "API interne pour les événements véhicules")
public class VehicleEventsListener {

    private final ContratUseCase contratUseCase;

    public VehicleEventsListener(ContratUseCase contratUseCase) {
        this.contratUseCase = contratUseCase;
    }

    @PostMapping("/marked-down")
    @Operation(
        summary = "Véhicule marqué en panne", 
        description = "Annule tous les contrats EN_ATTENTE du véhicule lorsqu'il est marqué en panne"
    )
    @ApiResponse(responseCode = "202", description = "Événement traité, contrats annulés")
    public ResponseEntity<VehicleMarkedDownResponse> handleVehicleMarkedDown(
            @RequestBody VehicleMarkedDownRequest request) {
        
        int canceledCount = contratUseCase.cancelPendingContractsForVehicle(request.vehiculeId());
        
        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(new VehicleMarkedDownResponse(request.vehiculeId(), canceledCount));
    }
    
    /**
     * DTO de requête pour l'événement "véhicule en panne".
     */
    public record VehicleMarkedDownRequest(UUID vehiculeId) {}
    
    /**
     * DTO de réponse pour l'événement "véhicule en panne".
     */
    public record VehicleMarkedDownResponse(UUID vehiculeId, int contractsCanceled) {}
}
