package com.bfb.interfaces.events;

import com.bfb.business.contract.service.ContractService;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Internal REST listener for vehicle-related events.
 * These endpoints are designed to be called by other internal services.
 * Hidden from public Swagger documentation for security reasons.
 */
@RestController
@RequestMapping("/internal/events/vehicles")
@Hidden
public class VehicleEventsListener {

    private final ContractService contractService;

    public VehicleEventsListener(ContractService contractService) {
        this.contractService = contractService;
    }

    @PostMapping("/marked-down")
    public ResponseEntity<VehicleMarkedDownResponse> handleVehicleMarkedDown(
            @Valid @RequestBody VehicleMarkedDownRequest request) {
        
        int canceledCount = contractService.cancelPendingContractsForVehicle(request.vehicleId());
        
        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(new VehicleMarkedDownResponse(request.vehicleId(), canceledCount));
    }
    
    /**
     * Request DTO for "vehicle marked as broken" event.
     */
    public record VehicleMarkedDownRequest(
        @NotNull(message = "Vehicle ID is required")
        UUID vehicleId
    ) {}
    
    /**
     * Response DTO for "vehicle marked as broken" event.
     */
    public record VehicleMarkedDownResponse(UUID vehicleId, int contractsCancelled) {}
}
