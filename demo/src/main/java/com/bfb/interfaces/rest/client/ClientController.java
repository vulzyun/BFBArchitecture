package com.bfb.interfaces.rest.client;

import com.bfb.business.client.model.Client;
import com.bfb.business.client.service.ClientService;
import com.bfb.interfaces.rest.client.dto.ClientDto;
import com.bfb.interfaces.rest.client.dto.CreateClientRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
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

    @PostMapping
    @Operation(summary = "Create a new client")
    public ResponseEntity<ClientDto> create(@Valid @RequestBody CreateClientRequest request) {
        Client client = clientService.create(request.name(), request.email());
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(toDto(client));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get client by ID")
    public ResponseEntity<ClientDto> getById(@PathVariable UUID id) {
        Client client = clientService.findById(id);
        return ResponseEntity.ok(toDto(client));
    }

    @GetMapping
    @Operation(summary = "Get all clients", description = "Retrieves the list of all clients")
    public ResponseEntity<List<ClientDto>> getAll() {
        List<Client> clients = clientService.findAll();
        List<ClientDto> dtos = clients.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a client")
    public ResponseEntity<ClientDto> update(
        @PathVariable UUID id, 
        @Valid @RequestBody CreateClientRequest request
    ) {
        Client client = clientService.update(id, request.name(), request.email());
        return ResponseEntity.ok(toDto(client));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a client")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private ClientDto toDto(Client client) {
        return new ClientDto(
            client.getId(),
            client.getName(),
            client.getEmail()
        );
    }
}
