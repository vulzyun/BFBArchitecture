package com.bfb.interfaces.rest.vehicle;

import com.bfb.business.vehicle.model.Vehicle;
import com.bfb.business.vehicle.model.VehicleStatus;
import com.bfb.business.vehicle.service.VehicleService;
import com.bfb.interfaces.rest.common.BaseRestController;
import com.bfb.interfaces.rest.vehicle.dto.CreateVehicleRequest;
import com.bfb.interfaces.rest.vehicle.dto.VehicleDto;
import com.bfb.interfaces.rest.vehicle.mapper.VehicleMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vehicles")
@Tag(name = "Vehicles", description = "Vehicle management API")
public class VehicleController extends BaseRestController<Vehicle, VehicleDto> {

    private final VehicleService vehicleService;
    private final VehicleMapper vehicleMapper;

    public VehicleController(VehicleService vehicleService, VehicleMapper vehicleMapper) {
        this.vehicleService = vehicleService;
        this.vehicleMapper = vehicleMapper;
    }

    @PostMapping
    @Operation(summary = "Create a new vehicle")
    public ResponseEntity<VehicleDto> create(@Valid @RequestBody CreateVehicleRequest request) {
        Vehicle vehicle = vehicleService.create(request.brand(), request.model(), request.motorization(), request.color(), request.registrationPlate(), request.purchaseDate());
        return created(vehicleMapper.toDto(vehicle));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get vehicle by ID")
    public ResponseEntity<VehicleDto> getById(@PathVariable UUID id) {
        Vehicle vehicle = vehicleService.findById(id);
        return ok(vehicleMapper.toDto(vehicle));
    }

    @GetMapping
    @Operation(summary = "Get all vehicles")
    public ResponseEntity<org.springframework.data.domain.Page<VehicleDto>> getAll(
        @RequestParam(required = false) VehicleStatus status,
        org.springframework.data.domain.Pageable pageable
    ) {
        org.springframework.data.domain.Page<Vehicle> vehicles = status != null 
            ? vehicleService.findByStatus(status, pageable)
            : vehicleService.findAll(pageable);
        org.springframework.data.domain.Page<VehicleDto> dtos = vehicles.map(vehicleMapper::toDto);
        return okPage(dtos);
    }

    @PatchMapping("/{id}/mark-broken")
    @Operation(summary = "Mark vehicle as broken")
    public ResponseEntity<VehicleDto> markAsBroken(@PathVariable UUID id) {
        Vehicle vehicle = vehicleService.markAsBroken(id);
        return ok(vehicleMapper.toDto(vehicle));
    }

    @PatchMapping("/{id}/mark-available")
    @Operation(summary = "Mark vehicle as available")
    public ResponseEntity<VehicleDto> markAsAvailable(@PathVariable UUID id) {
        Vehicle vehicle = vehicleService.markAsAvailable(id);
        return ok(vehicleMapper.toDto(vehicle));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a vehicle")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        vehicleService.delete(id);
        return noContent();
    }
}
