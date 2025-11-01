package com.BFBManagement.business.contrats;

import com.BFBManagement.architecture.contrats.domain.Contrat;
import com.BFBManagement.architecture.contrats.domain.ContratRepository;
import com.BFBManagement.architecture.contrats.domain.EtatContrat;
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

    // ==================== Tests cancelPendingContractsForVehicle() ====================

    @Test
    void cancel_pending_when_vehicle_marked_down_cancels_only_EN_ATTENTE() {
        // Given: 3 contrats même véhicule: A EN_ATTENTE, B EN_COURS, C EN_RETARD
        UUID vehiculeId = UUID.randomUUID();
        
        Contrat contratA = new Contrat();
        contratA.setId(UUID.randomUUID());
        contratA.setVehiculeId(vehiculeId);
        contratA.setEtat(EtatContrat.EN_ATTENTE);
        
        Contrat contratB = new Contrat();
        contratB.setId(UUID.randomUUID());
        contratB.setVehiculeId(vehiculeId);
        contratB.setEtat(EtatContrat.EN_COURS);
        
        Contrat contratC = new Contrat();
        contratC.setId(UUID.randomUUID());
        contratC.setVehiculeId(vehiculeId);
        contratC.setEtat(EtatContrat.EN_RETARD);
        
        when(contratRepository.findByVehiculeIdAndEtat(vehiculeId, EtatContrat.EN_ATTENTE))
            .thenReturn(List.of(contratA));
        when(contratRepository.save(any(Contrat.class))).thenAnswer(inv -> inv.getArgument(0));
        
        // When: cancelPendingContractsForVehicle(vehiculeId)
        contratService.cancelPendingContractsForVehicle(vehiculeId);
        
        // Then: A→ANNULE, B et C inchangés
        verify(contratRepository).save(argThat(c -> 
            c.getId().equals(contratA.getId()) && c.getEtat() == EtatContrat.ANNULE
        ));
        verify(contratRepository, times(1)).save(any(Contrat.class));
    }

    // ==================== Tests markLateAndCancelBlocked() ====================

    @Test
    void mark_late_moves_inprogress_to_late_when_past_enddate() {
        // Given: contrat EN_COURS avec dateFin hier
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Contrat contrat = new Contrat();
        contrat.setId(UUID.randomUUID());
        contrat.setVehiculeId(UUID.randomUUID());
        contrat.setEtat(EtatContrat.EN_COURS);
        contrat.setDateFin(yesterday);
        
        when(contratRepository.findByEtat(EtatContrat.EN_COURS))
            .thenReturn(List.of(contrat));
        when(contratRepository.findByVehiculeIdAndEtat(any(), eq(EtatContrat.EN_ATTENTE)))
            .thenReturn(List.of());
        when(contratRepository.save(any(Contrat.class))).thenAnswer(inv -> inv.getArgument(0));
        
        // When
        contratService.markLateAndCancelBlocked();
        
        // Then: contrat doit passer à EN_RETARD
        verify(contratRepository).save(argThat(c -> 
            c.getEtat() == EtatContrat.EN_RETARD
        ));
    }

    @Test
    void mark_late_cancels_next_awaiting_if_blocked_on_same_vehicle() {
        // Given: A EN_COURS fin hier, B EN_ATTENTE début aujourd'hui, même véhicule
        UUID vehiculeId = UUID.randomUUID();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate today = LocalDate.now();
        
        Contrat contratA = new Contrat();
        contratA.setId(UUID.randomUUID());
        contratA.setVehiculeId(vehiculeId);
        contratA.setEtat(EtatContrat.EN_COURS);
        contratA.setDateFin(yesterday);
        
        Contrat contratB = new Contrat();
        contratB.setId(UUID.randomUUID());
        contratB.setVehiculeId(vehiculeId);
        contratB.setEtat(EtatContrat.EN_ATTENTE);
        contratB.setDateDebut(today);
        contratB.setDateFin(today.plusDays(5));
        
        when(contratRepository.findByEtat(EtatContrat.EN_COURS))
            .thenReturn(List.of(contratA));
        when(contratRepository.findByVehiculeIdAndEtat(vehiculeId, EtatContrat.EN_ATTENTE))
            .thenReturn(List.of(contratB));
        when(contratRepository.save(any(Contrat.class))).thenAnswer(inv -> inv.getArgument(0));
        
        // When: markLateAndCancelBlocked()
        contratService.markLateAndCancelBlocked();
        
        // Then: A→EN_RETARD, B→ANNULE
        verify(contratRepository, times(2)).save(any(Contrat.class));
        verify(contratRepository).save(argThat(c -> 
            c.getId().equals(contratA.getId()) && c.getEtat() == EtatContrat.EN_RETARD
        ));
        verify(contratRepository).save(argThat(c -> 
            c.getId().equals(contratB.getId()) && c.getEtat() == EtatContrat.ANNULE
        ));
    }

    @Test
    void mark_late_does_not_cancel_unrelated_or_future() {
        // Given: A EN_COURS fin hier, B EN_ATTENTE commence dans 10j, même véhicule
        UUID vehiculeId = UUID.randomUUID();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate futureDate = LocalDate.now().plusDays(10);
        
        Contrat contratA = new Contrat();
        contratA.setId(UUID.randomUUID());
        contratA.setVehiculeId(vehiculeId);
        contratA.setEtat(EtatContrat.EN_COURS);
        contratA.setDateFin(yesterday);
        
        Contrat contratB = new Contrat();
        contratB.setId(UUID.randomUUID());
        contratB.setVehiculeId(vehiculeId);
        contratB.setEtat(EtatContrat.EN_ATTENTE);
        contratB.setDateDebut(futureDate);
        contratB.setDateFin(futureDate.plusDays(5));
        
        when(contratRepository.findByEtat(EtatContrat.EN_COURS))
            .thenReturn(List.of(contratA));
        when(contratRepository.findByVehiculeIdAndEtat(vehiculeId, EtatContrat.EN_ATTENTE))
            .thenReturn(List.of(contratB));
        when(contratRepository.save(any(Contrat.class))).thenAnswer(inv -> inv.getArgument(0));
        
        // When
        contratService.markLateAndCancelBlocked();
        
        // Then: seul A est modifié (EN_RETARD), B reste EN_ATTENTE
        verify(contratRepository, times(1)).save(any(Contrat.class));
        verify(contratRepository).save(argThat(c -> 
            c.getId().equals(contratA.getId()) && c.getEtat() == EtatContrat.EN_RETARD
        ));
        // B ne doit pas être sauvegardé
        verify(contratRepository, never()).save(argThat(c -> 
            c.getId().equals(contratB.getId())
        ));
    }
}
