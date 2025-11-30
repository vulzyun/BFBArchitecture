package com.bfb.interfaces.rest.contract;

import com.bfb.business.contract.model.Contract;
import com.bfb.business.contract.model.ContractStatus;
import com.bfb.business.contract.service.ContractService;
import com.bfb.interfaces.rest.contract.dto.ContractDto;
import com.bfb.interfaces.rest.contract.dto.CreateContractRequest;
import com.bfb.interfaces.rest.contract.dto.MarkLateResponse;
import com.bfb.interfaces.rest.contract.mapper.ContractMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for contract management.
 */
@RestController
@RequestMapping("/api/v1/contracts")
@Tag(name = "Contracts", description = "Contract management API (v1)")
public class ContractController {

    private final ContractService contractService;
    private final ContractMapper contractMapper;

    public ContractController(ContractService contractService, ContractMapper contractMapper) {
        this.contractService = contractService;
        this.contractMapper = contractMapper;
    }

    @PostMapping
    @Operation(
        summary = "Create a new contract",
        description = """
            Creates a new rental contract with full business rule validation.
            
            **Validations performed:**
            - Dates are coherent (startDate < endDate)
            - No overlap with other contracts for this vehicle
            - Vehicle is available (not broken)
            - Client exists
            
            The contract is created with PENDING status.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Contract created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid data (incoherent dates, missing fields)"),
        @ApiResponse(responseCode = "409", description = "Business conflict (date overlap or vehicle unavailable)")
    })
    public ResponseEntity<ContractDto> create(@Valid @RequestBody CreateContractRequest dto) {
        Contract contract = contractService.create(
            dto.clientId(),
            dto.vehicleId(),
            dto.startDate(),
            dto.endDate()
        );
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(contractMapper.toDto(contract));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get contract by ID",
        description = "Retrieves and returns a specific contract by its unique identifier"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contract found and returned successfully"),
        @ApiResponse(responseCode = "404", description = "Contract not found with this identifier")
    })
    public ResponseEntity<ContractDto> getById(@PathVariable UUID id) {
        Contract contract = contractService.findById(id);
        return ResponseEntity.ok(contractMapper.toDto(contract));
    }

    @GetMapping
    @Operation(
        summary = "Search contracts",
        description = """
            Multi-criteria contract search with pagination support. All parameters are optional.
            
            **Usage examples:**
            - No parameters: returns all contracts (paginated)
            - With clientId: all contracts for a client
            - With vehicleId: all contracts for a vehicle
            - With status: all contracts in a specific status
            - Combinations possible: clientId + status, vehicleId + status, etc.
            - Pagination: ?page=0&size=10&sort=startDate,desc
            """
    )
    public ResponseEntity<org.springframework.data.domain.Page<ContractDto>> search(
            @io.swagger.v3.oas.annotations.Parameter(
                description = "Client identifier (optional)",
                example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @RequestParam(required = false) UUID clientId,
            @io.swagger.v3.oas.annotations.Parameter(
                description = "Vehicle identifier (optional)",
                example = "987fcdeb-51a2-43d7-b123-987654321abc"
            )
            @RequestParam(required = false) UUID vehicleId,
            @io.swagger.v3.oas.annotations.Parameter(
                description = "Contract status (optional)",
                example = "IN_PROGRESS"
            )
            @RequestParam(required = false) ContractStatus status,
            @io.swagger.v3.oas.annotations.Parameter(
                description = "Pagination parameters (page, size, sort)",
                example = "page=0&size=20&sort=startDate,desc"
            )
            org.springframework.data.domain.Pageable pageable
    ) {
        org.springframework.data.domain.Page<Contract> contracts = contractService.findByCriteria(
            clientId, vehicleId, status, pageable
        );
        org.springframework.data.domain.Page<ContractDto> dtos = contracts.map(contractMapper::toDto);
        return ResponseEntity.ok(dtos);
    }

    @PatchMapping("/{id}/start")
    @Operation(
        summary = "Start a contract",
        description = """
            Transitions the contract from PENDING to IN_PROGRESS status.
            
            This operation marks the actual start of the rental.
            **Allowed transition only:** PENDING → IN_PROGRESS
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contract started successfully"),
        @ApiResponse(responseCode = "404", description = "Contract not found"),
        @ApiResponse(responseCode = "422", description = "Transition not allowed (contract is not in PENDING status)")
    })
    public ResponseEntity<ContractDto> start(@PathVariable UUID id) {
        Contract contract = contractService.start(id);
        return ResponseEntity.ok(contractMapper.toDto(contract));
    }

    @PatchMapping("/{id}/terminate")
    @Operation(
        summary = "Terminate a contract",
        description = """
            Terminates an in-progress or late contract.
            
            This operation marks the end of the rental and frees the vehicle.
            **Allowed transitions:** IN_PROGRESS → COMPLETED or LATE → COMPLETED
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contract terminated successfully"),
        @ApiResponse(responseCode = "404", description = "Contract not found"),
        @ApiResponse(responseCode = "422", description = "Transition not allowed (contract is neither IN_PROGRESS nor LATE)")
    })
    public ResponseEntity<ContractDto> terminate(@PathVariable UUID id) {
        Contract contract = contractService.terminate(id);
        return ResponseEntity.ok(contractMapper.toDto(contract));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(
        summary = "Cancel a contract",
        description = """
            Cancels a contract that has not started yet.
            
            This operation allows cancelling a reservation before rental begins.
            **Allowed transition only:** PENDING → CANCELLED
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contract cancelled successfully"),
        @ApiResponse(responseCode = "404", description = "Contract not found"),
        @ApiResponse(responseCode = "422", description = "Transition not allowed (contract is not in PENDING status)")
    })
    public ResponseEntity<ContractDto> cancel(@PathVariable UUID id) {
        Contract contract = contractService.cancel(id);
        return ResponseEntity.ok(contractMapper.toDto(contract));
    }

    @PostMapping("/jobs/mark-late")
    @Operation(
        summary = "Job: mark late contracts",
        description = """
            Automatic job that marks IN_PROGRESS contracts as LATE when endDate is past.
            
            This endpoint can be called manually or via a scheduler (CRON).
            It processes all IN_PROGRESS contracts and checks if the end date is past.
            
            **Transition performed:** IN_PROGRESS → LATE (if endDate < today)
            
            Returns the number of modified contracts.
            """
    )
    @ApiResponse(
        responseCode = "202",
        description = "Job executed successfully. Number of contracts marked LATE is returned in response."
    )
    public ResponseEntity<MarkLateResponse> markLate() {
        int count = contractService.markLateIfOverdue();
        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(new MarkLateResponse(count));
    }
}
