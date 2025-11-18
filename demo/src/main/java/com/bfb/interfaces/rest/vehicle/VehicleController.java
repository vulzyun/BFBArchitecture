package com.bfb.interfaces.rest.vehicle;

import com.bfb.business.vehicle.model.Vehicle;
import com.bfb.business.vehicle.service.VehicleService;
import com.bfb.interfaces.rest.vehicle.dto.VehicleDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @GetMapping
    @Operation(
        summary = "Get all vehicles",
        description = "Retrieves the list of all vehicles"
    )
    public ResponseEntity<List<VehicleDto>> getAll() {
        List<Vehicle> vehicles = vehicleService.findAll();
        List<VehicleDto> dtos = vehicles.stream()
            .map(v -> new VehicleDto(v.getId(), v.getBrand(), v.getModel(), v.getStatus()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
