package com.bfb.interfaces.rest.client.mapper;

import com.bfb.business.client.model.Client;
import com.bfb.interfaces.rest.client.dto.ClientDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * MapStruct mapper between Client domain model and ClientDto.
 * Automatically generates implementation at compile time.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ClientMapper {

    /**
     * Converts a Client domain entity to a ClientDto.
     * @param client the client entity
     * @return the client DTO
     */
    ClientDto toDto(Client client);
}
