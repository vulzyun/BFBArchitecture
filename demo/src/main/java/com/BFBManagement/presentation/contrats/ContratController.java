package com.BFBManagement.presentation.contrats;

import com.BFBManagement.architecture.contrats.domain.Contrat;
import com.BFBManagement.architecture.contrats.domain.EtatContrat;
import com.BFBManagement.business.contrats.ContratService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller REST pour la gestion des contrats de location.
 */
@RestController
@RequestMapping("/api/contrats")
@Tag(name = "Contrats", description = "API de gestion des contrats de location")
public class ContratController {

    private final ContratService contratService;
    private final ContratMapper contratMapper;

    public ContratController(ContratService contratService, ContratMapper contratMapper) {
        this.contratService = contratService;
        this.contratMapper = contratMapper;
    }

    @PostMapping
    @Operation(summary = "Créer un nouveau contrat", description = "Crée un nouveau contrat avec validation complète des règles métier")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Contrat créé avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides"),
        @ApiResponse(responseCode = "409", description = "Conflit (chevauchement ou véhicule indisponible)")
    })
    public ResponseEntity<ContratDto> create(@Valid @RequestBody CreateContratDto dto) {
        Contrat contrat = contratService.create(
            dto.clientId(),
            dto.vehiculeId(),
            dto.dateDebut(),
            dto.dateFin()
        );
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(contratMapper.toDto(contrat));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un contrat par ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contrat trouvé"),
        @ApiResponse(responseCode = "404", description = "Contrat introuvable")
    })
    public ResponseEntity<ContratDto> getById(@PathVariable UUID id) {
        Contrat contrat = contratService.findById(id);
        return ResponseEntity.ok(contratMapper.toDto(contrat));
    }

    @GetMapping
    @Operation(summary = "Rechercher des contrats", description = "Recherche par clientId, vehiculeId et/ou etat (tous optionnels)")
    public ResponseEntity<List<ContratDto>> search(
            @RequestParam(required = false) UUID clientId,
            @RequestParam(required = false) UUID vehiculeId,
            @RequestParam(required = false) EtatContrat etat
    ) {
        List<Contrat> contrats = contratService.findByCriteria(clientId, vehiculeId, etat);
        List<ContratDto> dtos = contrats.stream()
            .map(contratMapper::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PatchMapping("/{id}/start")
    @Operation(summary = "Démarrer un contrat", description = "Fait passer le contrat de EN_ATTENTE à EN_COURS")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contrat démarré"),
        @ApiResponse(responseCode = "404", description = "Contrat introuvable"),
        @ApiResponse(responseCode = "422", description = "Transition interdite")
    })
    public ResponseEntity<ContratDto> start(@PathVariable UUID id) {
        Contrat contrat = contratService.start(id);
        return ResponseEntity.ok(contratMapper.toDto(contrat));
    }

    @PatchMapping("/{id}/terminate")
    @Operation(summary = "Terminer un contrat", description = "Fait passer le contrat à TERMINE")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contrat terminé"),
        @ApiResponse(responseCode = "404", description = "Contrat introuvable"),
        @ApiResponse(responseCode = "422", description = "Transition interdite")
    })
    public ResponseEntity<ContratDto> terminate(@PathVariable UUID id) {
        Contrat contrat = contratService.terminate(id);
        return ResponseEntity.ok(contratMapper.toDto(contrat));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Annuler un contrat", description = "Fait passer le contrat de EN_ATTENTE à ANNULE")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contrat annulé"),
        @ApiResponse(responseCode = "404", description = "Contrat introuvable"),
        @ApiResponse(responseCode = "422", description = "Transition interdite")
    })
    public ResponseEntity<ContratDto> cancel(@PathVariable UUID id) {
        Contrat contrat = contratService.cancel(id);
        return ResponseEntity.ok(contratMapper.toDto(contrat));
    }

    @PostMapping("/jobs/mark-late")
    @Operation(summary = "Job : marquer les contrats en retard", description = "Marque automatiquement EN_RETARD les contrats EN_COURS dont la dateFin est dépassée")
    @ApiResponse(responseCode = "202", description = "Job exécuté, nombre de contrats modifiés retourné")
    public ResponseEntity<MarkLateResponse> markLate() {
        int count = contratService.markLateIfOverdue();
        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(new MarkLateResponse(count));
    }

    /**
     * DTO de réponse pour le job mark-late.
     */
    public record MarkLateResponse(int contratsMarkedLate) {}
}
