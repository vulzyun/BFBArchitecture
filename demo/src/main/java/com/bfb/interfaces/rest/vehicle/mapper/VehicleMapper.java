package com.bfb.interfaces.rest.vehicle.mapper;

import com.bfb.business.vehicle.model.Vehicle;
import com.bfb.interfaces.rest.vehicle.dto.VehicleDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * MapStruct mapper between Vehicle domain model and VehicleDto.
 * Automatically generates implementation at compile time.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface VehicleMapper {

    /**
     * Converts a Vehicle domain entity to a VehicleDto.
     * @param vehicle the vehicle entity
     * @return the vehicle DTO
     */
    VehicleDto toDto(Vehicle vehicle);
}
