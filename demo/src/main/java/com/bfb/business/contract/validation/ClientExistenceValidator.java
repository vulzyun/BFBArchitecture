package com.bfb.business.contract.validation;

import com.bfb.business.contract.exception.ClientUnknownException;
import com.bfb.business.client.service.ClientService;
import org.springframework.stereotype.Component;

@Component
public class ClientExistenceValidator implements ContractValidator {

    private final ClientService clientService;

    public ClientExistenceValidator(ClientService clientService) {
        this.clientService = clientService;
    }

    @Override
    public void validate(ContractCreationContext context) {
        if (!clientService.exists(context.getClientId())) {
            throw new ClientUnknownException(
                String.format("Client with ID '%s' not found. Ensure the client exists before creating a contract.", 
                    context.getClientId())
            );
        }
    }
}
