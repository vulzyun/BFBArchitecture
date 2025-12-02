package com.bfb.interfaces.rest.contract;

import com.bfb.business.contract.model.Contract;
import com.bfb.business.contract.model.ContractStatus;
import com.bfb.business.contract.service.ContractService;
import com.bfb.interfaces.rest.common.BaseRestController;
import com.bfb.interfaces.rest.contract.dto.ContractDto;
import com.bfb.interfaces.rest.contract.dto.CreateContractRequest;
import com.bfb.interfaces.rest.contract.dto.MarkLateResponse;
import com.bfb.interfaces.rest.contract.mapper.ContractMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/contracts")
@Tag(name = "Contracts", description = "Contract management API")
public class ContractController extends BaseRestController<Contract, ContractDto> {

    private final ContractService contractService;
    private final ContractMapper contractMapper;

    public ContractController(ContractService contractService, ContractMapper contractMapper) {
        this.contractService = contractService;
        this.contractMapper = contractMapper;
    }

    @PostMapping
    @Operation(summary = "Create a new contract")
    public ResponseEntity<ContractDto> create(@Valid @RequestBody CreateContractRequest dto) {
        Contract contract = contractService.create(
            dto.clientId(),
            dto.vehicleId(),
            dto.startDate(),
            dto.endDate()
        );
        return created(contractMapper.toDto(contract));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get contract by ID")
    public ResponseEntity<ContractDto> getById(@PathVariable UUID id) {
        Contract contract = contractService.findById(id);
        return ok(contractMapper.toDto(contract));
    }

    @GetMapping
    @Operation(summary = "Search contracts")
    public ResponseEntity<org.springframework.data.domain.Page<ContractDto>> search(
            @RequestParam(required = false) UUID clientId,
            @RequestParam(required = false) UUID vehicleId,
            @RequestParam(required = false) ContractStatus status,
            org.springframework.data.domain.Pageable pageable
    ) {
        org.springframework.data.domain.Page<Contract> contracts = contractService.findByCriteria(
            clientId, vehicleId, status, pageable
        );
        org.springframework.data.domain.Page<ContractDto> dtos = contracts.map(contractMapper::toDto);
        return okPage(dtos);
    }

    @PatchMapping("/{id}/start")
    @Operation(summary = "Start a contract")
    public ResponseEntity<ContractDto> start(@PathVariable UUID id) {
        Contract contract = contractService.start(id);
        return ok(contractMapper.toDto(contract));
    }

    @PatchMapping("/{id}/terminate")
    @Operation(summary = "Terminate a contract")
    public ResponseEntity<ContractDto> terminate(@PathVariable UUID id) {
        Contract contract = contractService.terminate(id);
        return ok(contractMapper.toDto(contract));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a contract")
    public ResponseEntity<ContractDto> cancel(@PathVariable UUID id) {
        Contract contract = contractService.cancel(id);
        return ok(contractMapper.toDto(contract));
    }

    @PostMapping("/jobs/mark-late")
    @Operation(summary = "Mark late contracts")
    public ResponseEntity<MarkLateResponse> markLate() {
        int count = contractService.markLateIfOverdue();
        return ResponseEntity.accepted().body(new MarkLateResponse(count));
    }
}
