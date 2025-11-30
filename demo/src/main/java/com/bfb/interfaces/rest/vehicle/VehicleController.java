package com.bfb.interfaces.rest.vehicle;

import com.bfb.business.vehicle.model.Vehicle;
import com.bfb.business.vehicle.model.VehicleStatus;
import com.bfb.business.vehicle.service.VehicleService;
import com.bfb.interfaces.rest.vehicle.dto.CreateVehicleRequest;
import com.bfb.interfaces.rest.vehicle.dto.VehicleDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for vehicle management.
 */
@RestController
@RequestMapping("/api/v1/vehicles")
@Tag(name = "Vehicles", description = "Vehicle management API (v1)")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    @Operation(summary = "Create a new vehicle")
    public ResponseEntity<VehicleDto> create(@Valid @RequestBody CreateVehicleRequest request) {
        Vehicle vehicle = vehicleService.create(request.brand(), request.model());
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(toDto(vehicle));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle by ID")
    public ResponseEntity<VehicleDto> getById(@PathVariable UUID id) {
        Vehicle vehicle = vehicleService.findById(id);
        return ResponseEntity.ok(toDto(vehicle));
    }

    @GetMapping
    @Operation(
        summary = "Get all vehicles", 
        description = "Retrieves the list of all vehicles with pagination support. Use ?page=0&size=20&sort=brand,asc for pagination."
    )
    public ResponseEntity<org.springframework.data.domain.Page<VehicleDto>> getAll(
        @RequestParam(required = false) VehicleStatus status,
        @io.swagger.v3.oas.annotations.Parameter(
            description = "Pagination parameters (page, size, sort)",
            example = "page=0&size=20&sort=brand,asc"
        )
        org.springframework.data.domain.Pageable pageable
    ) {
        org.springframework.data.domain.Page<Vehicle> vehicles = status != null 
            ? vehicleService.findByStatus(status, pageable)
            : vehicleService.findAll(pageable);
        org.springframework.data.domain.Page<VehicleDto> dtos = vehicles.map(this::toDto);
        return ResponseEntity.ok(dtos);
    }

    @PatchMapping("/{id}/mark-broken")
    @Operation(summary = "Mark vehicle as broken")
    public ResponseEntity<VehicleDto> markAsBroken(@PathVariable UUID id) {
        Vehicle vehicle = vehicleService.markAsBroken(id);
        return ResponseEntity.ok(toDto(vehicle));
    }

    @PatchMapping("/{id}/mark-available")
    @Operation(summary = "Mark vehicle as available")
    public ResponseEntity<VehicleDto> markAsAvailable(@PathVariable UUID id) {
        Vehicle vehicle = vehicleService.markAsAvailable(id);
        return ResponseEntity.ok(toDto(vehicle));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a vehicle")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        vehicleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private VehicleDto toDto(Vehicle vehicle) {
        return new VehicleDto(
            vehicle.getId(),
            vehicle.getBrand(),
            vehicle.getModel(),
            vehicle.getStatus()
        );
    }
}
