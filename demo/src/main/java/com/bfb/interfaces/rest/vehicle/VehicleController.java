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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for vehicle management.
 */
@RestController
@RequestMapping("/api/vehicles")
@Tag(name = "Vehicles", description = "Vehicle management API")
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
    @Operation(summary = "Get all vehicles", description = "Retrieves the list of all vehicles")
    public ResponseEntity<List<VehicleDto>> getAll(
        @RequestParam(required = false) VehicleStatus status
    ) {
        List<Vehicle> vehicles = status != null 
            ? vehicleService.findByStatus(status)
            : vehicleService.findAll();
        List<VehicleDto> dtos = vehicles.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
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
