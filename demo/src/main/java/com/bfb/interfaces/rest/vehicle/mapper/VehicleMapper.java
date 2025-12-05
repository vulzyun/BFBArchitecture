package com.bfb.interfaces.rest.vehicle.mapper;

import com.bfb.business.vehicle.model.Vehicle;
import com.bfb.interfaces.rest.vehicle.dto.VehicleDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface VehicleMapper {

    VehicleDto toDto(Vehicle vehicle);
}
