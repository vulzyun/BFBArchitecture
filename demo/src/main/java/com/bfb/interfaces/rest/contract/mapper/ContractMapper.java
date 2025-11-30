package com.bfb.interfaces.rest.contract.mapper;

import com.bfb.business.contract.model.Contract;
import com.bfb.interfaces.rest.contract.dto.ContractDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

/**
 * MapStruct mapper between Contract domain model and ContractDto.
 * Automatically generates implementation at compile time.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ContractMapper {

    /**
     * Converts a Contract domain entity to a ContractDto.
     * @param contract the contract entity
     * @return the contract DTO
     */
    ContractDto toDto(Contract contract);
}
