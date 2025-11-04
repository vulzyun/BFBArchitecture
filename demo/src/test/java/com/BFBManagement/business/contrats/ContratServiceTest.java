package com.BFBManagement.business.contrats;

import com.BFBManagement.infrastructure.contrats.domain.Contrat;
import com.BFBManagement.infrastructure.contrats.domain.ContratRepository;
import com.BFBManagement.infrastructure.contrats.domain.EtatContrat;
import com.BFBManagement.business.contrats.exceptions.*;
import com.BFBManagement.business.contrats.ports.ClientExistencePort;
import com.BFBManagement.business.contrats.ports.VehicleStatusPort;
import com.BFBManagement.business.vehicules.EtatVehicule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires du service métier Contrat.
 * TDD RED : ces tests doivent échouer tant que le service n'existe pas.
 */
@ExtendWith(MockitoExtension.class)
class ContratServiceTest {

    @Mock
    private ContratRepository contratRepository;

    @Mock
    private VehicleStatusPort vehicleStatusPort;

    @Mock
    private ClientExistencePort clientExistencePort;

    @InjectMocks
    private ContratService contratService;

    private UUID clientId;
    private UUID vehiculeId;
    private LocalDate dateDebut;
    private LocalDate dateFin;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        vehiculeId = UUID.randomUUID();
        dateDebut = LocalDate.of(2025, 12, 1);
        dateFin = LocalDate.of(2025, 12, 10);
    }

    // ==================== Tests create() ====================

    @Test
    void create_refuses_whenVehicleIsDown() {
        // Given : véhicule en panne
        when(clientExistencePort.existsById(clientId)).thenReturn(true);
        when(vehicleStatusPort.getStatus(vehiculeId)).thenReturn(EtatVehicule.EN_PANNE);

        // When/Then : doit lancer VehicleUnavailableException
        assertThrows(VehicleUnavailableException.class, () -> 
            contratService.create(clientId, vehiculeId, dateDebut, dateFin)
        );

        verify(contratRepository, never()).save(any());
    }

    @Test
    void create_refuses_whenDatesInvalid() {
        // Given : dateDebut >= dateFin
        LocalDate invalidDateFin = dateDebut; // même date

        // When/Then : doit lancer ValidationException
        assertThrows(ValidationException.class, () -> 
            contratService.create(clientId, vehiculeId, dateDebut, invalidDateFin)
        );

        // Cas dateDebut > dateFin
        assertThrows(ValidationException.class, () -> 
            contratService.create(clientId, vehiculeId, dateDebut, dateDebut.minusDays(1))
        );
    }

    @Test
    void create_refuses_whenClientUnknown() {
        // Given : client inexistant
        when(clientExistencePort.existsById(clientId)).thenReturn(false);

        // When/Then : doit lancer ClientUnknownException
        assertThrows(ClientUnknownException.class, () -> 
            contratService.create(clientId, vehiculeId, dateDebut, dateFin)
        );
    }

    @Test
    void create_refuses_whenOverlap() {
        // Given : contrat existant qui chevauche
        when(clientExistencePort.existsById(clientId)).thenReturn(true);
        when(vehicleStatusPort.getStatus(vehiculeId)).thenReturn(EtatVehicule.DISPONIBLE);

        Contrat existingContrat = new Contrat();
        existingContrat.setVehiculeId(vehiculeId);
        existingContrat.setDateDebut(LocalDate.of(2025, 12, 5));
        existingContrat.setDateFin(LocalDate.of(2025, 12, 15));
        existingContrat.setEtat(EtatContrat.EN_COURS);

        when(contratRepository.findOverlappingContrats(eq(vehiculeId), any(), any()))
            .thenReturn(List.of(existingContrat));

        // When/Then : doit lancer OverlapException
        assertThrows(OverlapException.class, () -> 
            contratService.create(clientId, vehiculeId, dateDebut, dateFin)
        );
    }

    @Test
    void create_succeeds_whenAllConditionsMet() {
        // Given : tout est OK
        when(clientExistencePort.existsById(clientId)).thenReturn(true);
        when(vehicleStatusPort.getStatus(vehiculeId)).thenReturn(EtatVehicule.DISPONIBLE);
        when(contratRepository.findOverlappingContrats(eq(vehiculeId), any(), any()))
            .thenReturn(List.of()); // Pas de chevauchement

        Contrat savedContrat = new Contrat();
        savedContrat.setId(UUID.randomUUID());
        savedContrat.setEtat(EtatContrat.EN_ATTENTE);
        when(contratRepository.save(any(Contrat.class))).thenReturn(savedContrat);

        // When
        Contrat result = contratService.create(clientId, vehiculeId, dateDebut, dateFin);

        // Then
        assertNotNull(result);
        assertEquals(EtatContrat.EN_ATTENTE, result.getEtat());
        verify(contratRepository).save(any(Contrat.class));
    }

    // ==================== Tests start() ====================

    @Test
    void start_allows_from_EN_ATTENTE_only() {
        // Given : contrat EN_ATTENTE
        UUID contratId = UUID.randomUUID();
        Contrat contrat = new Contrat();
        contrat.setId(contratId);
        contrat.setEtat(EtatContrat.EN_ATTENTE);
        when(contratRepository.findById(contratId)).thenReturn(Optional.of(contrat));
        when(contratRepository.save(any(Contrat.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Contrat result = contratService.start(contratId);

        // Then
        assertEquals(EtatContrat.EN_COURS, result.getEtat());
    }

    @Test
    void start_refuses_from_other_states() {
        UUID contratId = UUID.randomUUID();

        // EN_COURS → EN_COURS devrait être OK (idempotent), mais start vers EN_COURS depuis EN_COURS
        // est techniquement une transition du même état
        Contrat contratEnCours = new Contrat();
        contratEnCours.setId(contratId);
        contratEnCours.setEtat(EtatContrat.EN_COURS);
        when(contratRepository.findById(contratId)).thenReturn(Optional.of(contratEnCours));

        // C'est idempotent selon nos règles, donc pas d'erreur
        assertDoesNotThrow(() -> contratService.start(contratId));

        // TERMINE → EN_COURS : interdit
        Contrat contratTermine = new Contrat();
        contratTermine.setId(contratId);
        contratTermine.setEtat(EtatContrat.TERMINE);
        when(contratRepository.findById(contratId)).thenReturn(Optional.of(contratTermine));

        assertThrows(TransitionNotAllowedException.class, () -> 
            contratService.start(contratId)
        );
    }

    // ==================== Tests terminate() ====================

    @Test
    void terminate_allows_from_EN_COURS_only() {
        UUID contratId = UUID.randomUUID();
        Contrat contrat = new Contrat();
        contrat.setId(contratId);
        contrat.setEtat(EtatContrat.EN_COURS);
        when(contratRepository.findById(contratId)).thenReturn(Optional.of(contrat));
        when(contratRepository.save(any(Contrat.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Contrat result = contratService.terminate(contratId);

        // Then
        assertEquals(EtatContrat.TERMINE, result.getEtat());
    }

    @Test
    void terminate_allows_from_EN_RETARD() {
        UUID contratId = UUID.randomUUID();
        Contrat contrat = new Contrat();
        contrat.setId(contratId);
        contrat.setEtat(EtatContrat.EN_RETARD);
        when(contratRepository.findById(contratId)).thenReturn(Optional.of(contrat));
        when(contratRepository.save(any(Contrat.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Contrat result = contratService.terminate(contratId);

        // Then
        assertEquals(EtatContrat.TERMINE, result.getEtat());
    }

    @Test
    void terminate_refuses_from_EN_ATTENTE() {
        UUID contratId = UUID.randomUUID();
        Contrat contrat = new Contrat();
        contrat.setId(contratId);
        contrat.setEtat(EtatContrat.EN_ATTENTE);
        when(contratRepository.findById(contratId)).thenReturn(Optional.of(contrat));

        assertThrows(TransitionNotAllowedException.class, () -> 
            contratService.terminate(contratId)
        );
    }

    // ==================== Tests cancel() ====================

    @Test
    void cancel_allows_from_EN_ATTENTE_only() {
        UUID contratId = UUID.randomUUID();
        Contrat contrat = new Contrat();
        contrat.setId(contratId);
        contrat.setEtat(EtatContrat.EN_ATTENTE);
        when(contratRepository.findById(contratId)).thenReturn(Optional.of(contrat));
        when(contratRepository.save(any(Contrat.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        Contrat result = contratService.cancel(contratId);

        // Then
        assertEquals(EtatContrat.ANNULE, result.getEtat());
    }

    @Test
    void cancel_refuses_from_EN_COURS() {
        UUID contratId = UUID.randomUUID();
        Contrat contrat = new Contrat();
        contrat.setId(contratId);
        contrat.setEtat(EtatContrat.EN_COURS);
        when(contratRepository.findById(contratId)).thenReturn(Optional.of(contrat));

        assertThrows(TransitionNotAllowedException.class, () -> 
            contratService.cancel(contratId)
        );
    }

    // ==================== Tests markLateIfOverdue() ====================

    @Test
    void markLateIfOverdue_moves_EN_COURS_to_EN_RETARD_whenPastEndDate() {
        // Given : contrat EN_COURS avec dateFin hier
        Contrat contrat = new Contrat();
        contrat.setId(UUID.randomUUID());
        contrat.setEtat(EtatContrat.EN_COURS);
        contrat.setDateFin(LocalDate.now().minusDays(1));
        
        when(contratRepository.findByEtat(EtatContrat.EN_COURS))
            .thenReturn(List.of(contrat));
        when(contratRepository.save(any(Contrat.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        contratService.markLateIfOverdue();

        // Then
        verify(contratRepository).save(argThat(c -> 
            c.getEtat() == EtatContrat.EN_RETARD
        ));
    }

    @Test
    void markLateIfOverdue_doesNotChange_whenNotOverdue() {
        // Given : contrat EN_COURS avec dateFin demain
        Contrat contrat = new Contrat();
        contrat.setId(UUID.randomUUID());
        contrat.setEtat(EtatContrat.EN_COURS);
        contrat.setDateFin(LocalDate.now().plusDays(1));
        
        when(contratRepository.findByEtat(EtatContrat.EN_COURS))
            .thenReturn(List.of(contrat));

        // When
        contratService.markLateIfOverdue();

        // Then : aucune sauvegarde ne doit être faite
        verify(contratRepository, never()).save(any());
    }

    @Test
    void findById_throws_when_not_found() {
        UUID contratId = UUID.randomUUID();
        when(contratRepository.findById(contratId)).thenReturn(Optional.empty());

        assertThrows(ContratNotFoundException.class, () -> 
            contratService.start(contratId)
        );
    }
}
