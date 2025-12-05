package com.bfb.interfaces.rest.contract.mapper;

import com.bfb.business.contract.model.Contract;
import com.bfb.interfaces.rest.contract.dto.ContractDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ContractMapper {

    ContractDto toDto(Contract contract);
}
