package com.bfb.business.contract.validation;

import com.bfb.business.contract.exception.ClientUnknownException;
import com.bfb.business.contract.service.ClientExistencePort;
import org.springframework.stereotype.Component;

/**
 * Validates that the client exists in the system.
 */
@Component
public class ClientExistenceValidator implements ContractValidator {

    private final ClientExistencePort clientExistencePort;

    public ClientExistenceValidator(ClientExistencePort clientExistencePort) {
        this.clientExistencePort = clientExistencePort;
    }

    @Override
    public void validate(ContractCreationContext context) {
        if (!clientExistencePort.existsById(context.getClientId())) {
            throw new ClientUnknownException(
                String.format("Client with ID '%s' not found. Ensure the client exists before creating a contract.", 
                    context.getClientId())
            );
        }
    }
}
