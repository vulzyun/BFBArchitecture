package com.bfb.interfaces.rest.client;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bfb.business.client.model.Client;
import com.bfb.business.client.service.ClientService;
import com.bfb.interfaces.rest.client.dto.ClientDto;
import com.bfb.interfaces.rest.client.dto.CreateClientRequest;
import com.bfb.interfaces.rest.client.mapper.ClientMapper;
import com.bfb.interfaces.rest.common.BaseRestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST controller for client management.
 * Extends BaseRestController for consistent response handling.
 */
@RestController
@RequestMapping("/api/v1/clients")
@Tag(name = "Clients", description = "Client management API (v1)")
public class ClientController extends BaseRestController<Client, ClientDto> {

    private final ClientService clientService;
    private final ClientMapper clientMapper;

    public ClientController(ClientService clientService, ClientMapper clientMapper) {
        this.clientService = clientService;
        this.clientMapper = clientMapper;
    }

    @PostMapping
    @Operation(summary = "Create a new client")
    public ResponseEntity<ClientDto> create(@Valid @RequestBody CreateClientRequest request) {
        Client client = clientService.create(request.prenom(), request.nom(), request.adresse(), request.numPermis(), request.dateNaissance());
        return created(clientMapper.toDto(client));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get client by ID")
    public ResponseEntity<ClientDto> getById(@PathVariable UUID id) {
        Client client = clientService.findById(id);
        return ok(clientMapper.toDto(client));
    }

    @GetMapping
    @Operation(
        summary = "Get all clients", 
        description = "Retrieves the list of all clients with pagination support"
    )
    public ResponseEntity<org.springframework.data.domain.Page<ClientDto>> getAll(
        @RequestParam(defaultValue = "0") 
        @io.swagger.v3.oas.annotations.Parameter(description = "Page number (0-based)") 
        int page,
        
        @RequestParam(defaultValue = "20") 
        @io.swagger.v3.oas.annotations.Parameter(description = "Page size") 
        int size,
        
        @RequestParam(defaultValue = "nom,asc") 
        @io.swagger.v3.oas.annotations.Parameter(description = "Sort criteria (field,direction)") 
        String sort
    ) {
        // Parse sort parameter
        String[] sortParams = sort.split(",");
        org.springframework.data.domain.Sort.Direction direction = 
            sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc") 
                ? org.springframework.data.domain.Sort.Direction.DESC 
                : org.springframework.data.domain.Sort.Direction.ASC;
        
        org.springframework.data.domain.Pageable pageable = 
            org.springframework.data.domain.PageRequest.of(page, size, direction, sortParams[0]);
        
        org.springframework.data.domain.Page<Client> clients = clientService.findAll(pageable);
        org.springframework.data.domain.Page<ClientDto> dtos = clients.map(clientMapper::toDto);
        return okPage(dtos);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a client")
    public ResponseEntity<ClientDto> update(
        @PathVariable UUID id, 
        @Valid @RequestBody CreateClientRequest request
    ) {
        Client client = clientService.update(id, request.prenom(), request.nom(), request.adresse(), request.numPermis(), request.dateNaissance());
        return ok(clientMapper.toDto(client));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a client")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        clientService.delete(id);
        return noContent();
    }
}
