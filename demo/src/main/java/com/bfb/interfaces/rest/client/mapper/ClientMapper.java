package com.bfb.interfaces.rest.client.mapper;

import com.bfb.business.client.model.Client;
import com.bfb.interfaces.rest.client.dto.ClientDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ClientMapper {
    ClientDto toDto(Client client);
}
