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
    @Operation(
        summary = "Créer un nouveau contrat",
        description = """
            Crée un nouveau contrat de location avec validation complète des règles métier.
            
            **Validations effectuées :**
            - Dates cohérentes (dateDebut < dateFin)
            - Pas de chevauchement avec d'autres contrats pour ce véhicule
            - Véhicule disponible (non en panne)
            - Client existant
            
            Le contrat est créé avec l'état EN_ATTENTE.
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Contrat créé avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides (dates incohérentes, champs manquants)"),
        @ApiResponse(responseCode = "409", description = "Conflit métier (chevauchement de dates ou véhicule indisponible)")
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
    @Operation(
        summary = "Récupérer un contrat par ID",
        description = "Recherche et retourne un contrat spécifique par son identifiant unique"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contrat trouvé et retourné avec succès"),
        @ApiResponse(responseCode = "404", description = "Contrat introuvable avec cet identifiant")
    })
    public ResponseEntity<ContratDto> getById(@PathVariable UUID id) {
        Contrat contrat = contratService.findById(id);
        return ResponseEntity.ok(contratMapper.toDto(contrat));
    }

    @GetMapping
    @Operation(
        summary = "Rechercher des contrats",
        description = """
            Recherche multicritère de contrats. Tous les paramètres sont optionnels.
            
            **Exemples d'utilisation :**
            - Sans paramètres : retourne tous les contrats
            - Avec clientId : tous les contrats d'un client
            - Avec vehiculeId : tous les contrats d'un véhicule
            - Avec etat : tous les contrats dans un état spécifique
            - Combinaisons possibles : clientId + etat, vehiculeId + etat, etc.
            """
    )
    public ResponseEntity<List<ContratDto>> search(
            @io.swagger.v3.oas.annotations.Parameter(
                description = "Identifiant du client (optionnel)",
                example = "123e4567-e89b-12d3-a456-426614174000"
            )
            @RequestParam(required = false) UUID clientId,
            @io.swagger.v3.oas.annotations.Parameter(
                description = "Identifiant du véhicule (optionnel)",
                example = "987fcdeb-51a2-43d7-b123-987654321abc"
            )
            @RequestParam(required = false) UUID vehiculeId,
            @io.swagger.v3.oas.annotations.Parameter(
                description = "État du contrat (optionnel)",
                example = "EN_COURS"
            )
            @RequestParam(required = false) EtatContrat etat
    ) {
        List<Contrat> contrats = contratService.findByCriteria(clientId, vehiculeId, etat);
        List<ContratDto> dtos = contrats.stream()
            .map(contratMapper::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PatchMapping("/{id}/start")
    @Operation(
        summary = "Démarrer un contrat",
        description = """
            Fait passer le contrat de l'état EN_ATTENTE à EN_COURS.
            
            Cette opération marque le début effectif de la location.
            **Transition autorisée uniquement :** EN_ATTENTE → EN_COURS
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contrat démarré avec succès"),
        @ApiResponse(responseCode = "404", description = "Contrat introuvable"),
        @ApiResponse(responseCode = "422", description = "Transition interdite (le contrat n'est pas en état EN_ATTENTE)")
    })
    public ResponseEntity<ContratDto> start(@PathVariable UUID id) {
        Contrat contrat = contratService.start(id);
        return ResponseEntity.ok(contratMapper.toDto(contrat));
    }

    @PatchMapping("/{id}/terminate")
    @Operation(
        summary = "Terminer un contrat",
        description = """
            Termine un contrat en cours ou en retard.
            
            Cette opération marque la fin effective de la location et libère le véhicule.
            **Transitions autorisées :** EN_COURS → TERMINE ou EN_RETARD → TERMINE
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contrat terminé avec succès"),
        @ApiResponse(responseCode = "404", description = "Contrat introuvable"),
        @ApiResponse(responseCode = "422", description = "Transition interdite (le contrat n'est ni EN_COURS ni EN_RETARD)")
    })
    public ResponseEntity<ContratDto> terminate(@PathVariable UUID id) {
        Contrat contrat = contratService.terminate(id);
        return ResponseEntity.ok(contratMapper.toDto(contrat));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(
        summary = "Annuler un contrat",
        description = """
            Annule un contrat qui n'a pas encore démarré.
            
            Cette opération permet d'annuler une réservation avant le début de la location.
            **Transition autorisée uniquement :** EN_ATTENTE → ANNULE
            """
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Contrat annulé avec succès"),
        @ApiResponse(responseCode = "404", description = "Contrat introuvable"),
        @ApiResponse(responseCode = "422", description = "Transition interdite (le contrat n'est pas en état EN_ATTENTE)")
    })
    public ResponseEntity<ContratDto> cancel(@PathVariable UUID id) {
        Contrat contrat = contratService.cancel(id);
        return ResponseEntity.ok(contratMapper.toDto(contrat));
    }

    @PostMapping("/jobs/mark-late")
    @Operation(
        summary = "Job : marquer les contrats en retard",
        description = """
            Job automatique qui marque EN_RETARD les contrats EN_COURS dont la dateFin est dépassée.
            
            Ce endpoint peut être appelé manuellement ou via un scheduler (CRON).
            Il parcourt tous les contrats EN_COURS et vérifie si la date de fin est dépassée.
            
            **Transition effectuée :** EN_COURS → EN_RETARD (si dateFin < aujourd'hui)
            
            Retourne le nombre de contrats modifiés.
            """
    )
    @ApiResponse(
        responseCode = "202",
        description = "Job exécuté avec succès. Le nombre de contrats marqués EN_RETARD est retourné dans la réponse."
    )
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
