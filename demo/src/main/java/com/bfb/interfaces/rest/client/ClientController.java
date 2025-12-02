package com.bfb.interfaces.rest.client;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/clients")
@Tag(name = "Clients", description = "Client management API")
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
        Client client = clientService.create(
            request.firstName(), 
            request.lastName(), 
            request.address(), 
            request.licenseNumber(), 
            request.birthDate()
        );
        return created(clientMapper.toDto(client));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get client by ID")
    public ResponseEntity<ClientDto> getById(@PathVariable UUID id) {
        Client client = clientService.findById(id);
        return ok(clientMapper.toDto(client));
    }

    @GetMapping
    @Operation(summary = "Get all clients")
    public ResponseEntity<Page<ClientDto>> getAll(
        @RequestParam(defaultValue = "0") @Parameter(description = "Page number") int page,
        @RequestParam(defaultValue = "20") @Parameter(description = "Page size") int size,
        @RequestParam(defaultValue = "lastName,asc") @Parameter(description = "Sort criteria") String sort
    ) {
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc") 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, direction, sortParams[0]);
        Page<Client> clients = clientService.findAll(pageable);
        return okPage(clients.map(clientMapper::toDto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a client")
    public ResponseEntity<ClientDto> update(@PathVariable UUID id, @Valid @RequestBody CreateClientRequest request) {
        Client client = clientService.update(
            id, 
            request.firstName(), 
            request.lastName(), 
            request.address(), 
            request.licenseNumber(), 
            request.birthDate()
        );
        return ok(clientMapper.toDto(client));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a client")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        clientService.delete(id);
        return noContent();
    }
}
