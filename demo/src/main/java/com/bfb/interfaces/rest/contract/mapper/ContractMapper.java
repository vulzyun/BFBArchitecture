package com.bfb.interfaces.rest.contract.mapper;

import com.bfb.business.contract.model.Contract;
import com.bfb.interfaces.rest.contract.dto.ContractDto;
import org.springframework.stereotype.Component;

/**
 * Mapper between Contract domain model and ContractDto.
 */
@Component
public class ContractMapper {

    public ContractDto toDto(Contract contract) {
        return new ContractDto(
            contract.getId(),
            contract.getClientId(),
            contract.getVehicleId(),
            contract.getStartDate(),
            contract.getEndDate(),
            contract.getStatus()
        );
    }
}
