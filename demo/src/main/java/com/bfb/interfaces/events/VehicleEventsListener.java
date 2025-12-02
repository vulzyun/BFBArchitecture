package com.bfb.interfaces.events;

import com.bfb.business.contract.service.ContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Internal REST listener for vehicle-related events.
 * These endpoints are designed to be called by other internal services.
 */
@RestController
@RequestMapping("/internal/events/vehicles")
@Tag(name = "Vehicle Events (Internal)", description = "Internal API for vehicle events")
public class VehicleEventsListener {

    private final ContractService contractService;

    public VehicleEventsListener(ContractService contractService) {
        this.contractService = contractService;
    }

    @PostMapping("/marked-down")
    @Operation(
        summary = "Vehicle marked as broken", 
        description = "Cancels all PENDING contracts for the vehicle when it's marked as broken"
    )
    @ApiResponse(responseCode = "202", description = "Event processed, contracts cancelled")
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
