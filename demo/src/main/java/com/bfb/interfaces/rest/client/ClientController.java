package com.bfb.interfaces.rest.client;

import com.bfb.business.client.model.Client;
import com.bfb.business.client.service.ClientService;
import com.bfb.interfaces.rest.client.dto.ClientDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for client management.
 */
@RestController
@RequestMapping("/api/clients")
@Tag(name = "Clients", description = "Client management API")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    @Operation(
        summary = "Get all clients",
        description = "Retrieves the list of all clients"
    )
    public ResponseEntity<List<ClientDto>> getAll() {
        List<Client> clients = clientService.findAll();
        List<ClientDto> dtos = clients.stream()
            .map(c -> new ClientDto(c.getId(), c.getName(), c.getEmail()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
