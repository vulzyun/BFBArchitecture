package com.BFBManagement.application.contrats;

import com.BFBManagement.domain.contrats.Contrat;
import com.BFBManagement.domain.contrats.EtatContrat;
import com.BFBManagement.domain.contrats.Rules;
import com.BFBManagement.domain.contrats.exceptions.*;
import com.BFBManagement.domain.vehicules.EtatVehicule;
import com.BFBManagement.application.contrats.ports.in.ContratUseCase;
import com.BFBManagement.application.contrats.ports.out.ClientExistencePort;
import com.BFBManagement.application.contrats.ports.out.ContratRepository;
import com.BFBManagement.application.contrats.ports.out.VehicleStatusPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Service métier pour la gestion des contrats de location.
 * Implémente toutes les règles métier et validations.
 */
@Service
@Transactional
public class ContratService implements ContratUseCase {

    private final ContratRepository contratRepository;
    private final VehicleStatusPort vehicleStatusPort;
    private final ClientExistencePort clientExistencePort;

    public ContratService(
            ContratRepository contratRepository,
            VehicleStatusPort vehicleStatusPort,
            ClientExistencePort clientExistencePort) {
        this.contratRepository = contratRepository;
        this.vehicleStatusPort = vehicleStatusPort;
        this.clientExistencePort = clientExistencePort;
    }

    @Override
    public Contrat create(UUID clientId, UUID vehiculeId, LocalDate dateDebut, LocalDate dateFin) {
        // 1. Valider les dates
        if (!dateDebut.isBefore(dateFin)) {
            throw new ValidationException(
                "La dateDebut doit être strictement antérieure à la dateFin"
            );
        }

        // 2. Vérifier que le client existe
        if (!clientExistencePort.existsById(clientId)) {
            throw new ClientUnknownException(
                String.format("Client %s introuvable", clientId)
            );
        }

        // 3. Vérifier que le véhicule n'est pas EN_PANNE
        EtatVehicule vehiculeStatus = vehicleStatusPort.getStatus(vehiculeId);
        if (vehiculeStatus == EtatVehicule.EN_PANNE) {
            throw new VehicleUnavailableException(
                String.format("Le véhicule %s est EN_PANNE et ne peut être loué", vehiculeId)
            );
        }

        // 4. Vérifier les chevauchements
        List<Contrat> overlappingContrats = contratRepository.findOverlappingContrats(
            vehiculeId, dateDebut, dateFin
        );
        
        if (!overlappingContrats.isEmpty()) {
            throw new OverlapException(
                String.format("Chevauchement détecté pour le véhicule %s sur la période %s - %s",
                    vehiculeId, dateDebut, dateFin)
            );
        }

        // 5. Créer et sauvegarder le contrat
        Contrat contrat = new Contrat(null, clientId, vehiculeId, dateDebut, dateFin, EtatContrat.EN_ATTENTE);
        return contratRepository.save(contrat);
    }

    @Override
    public Contrat start(UUID contratId) {
        Contrat contrat = findByIdOrThrow(contratId);
        
        if (!Rules.transitionAllowed(contrat.getEtat(), EtatContrat.EN_COURS)) {
            throw new TransitionNotAllowedException(
                String.format("Impossible de démarrer un contrat en état %s", contrat.getEtat())
            );
        }
        
        contrat.start();
        return contratRepository.save(contrat);
    }

    @Override
    public Contrat terminate(UUID contratId) {
        Contrat contrat = findByIdOrThrow(contratId);
        
        if (!Rules.transitionAllowed(contrat.getEtat(), EtatContrat.TERMINE)) {
            throw new TransitionNotAllowedException(
                String.format("Impossible de terminer un contrat en état %s", contrat.getEtat())
            );
        }
        
        contrat.terminate();
        return contratRepository.save(contrat);
    }

    @Override
    public Contrat cancel(UUID contratId) {
        Contrat contrat = findByIdOrThrow(contratId);
        
        if (!Rules.transitionAllowed(contrat.getEtat(), EtatContrat.ANNULE)) {
            throw new TransitionNotAllowedException(
                String.format("Impossible d'annuler un contrat en état %s", contrat.getEtat())
            );
        }
        
        contrat.cancel();
        return contratRepository.save(contrat);
    }

    @Override
    public int markLateIfOverdue() {
        LocalDate today = LocalDate.now();
        List<Contrat> contratsEnCours = contratRepository.findByEtat(EtatContrat.EN_COURS);
        
        int count = 0;
        for (Contrat contrat : contratsEnCours) {
            if (contrat.getDateFin().isBefore(today)) {
                contrat.markLate();
                contratRepository.save(contrat);
                count++;
            }
        }
        
        return count;
    }

    @Override
    public int cancelPendingContractsForVehicle(UUID vehiculeId) {
        List<Contrat> pendingContrats = contratRepository.findByVehiculeIdAndEtat(
            vehiculeId, 
            EtatContrat.EN_ATTENTE
        );
        
        int count = 0;
        for (Contrat contrat : pendingContrats) {
            contrat.cancel();
            contratRepository.save(contrat);
            count++;
        }
        
        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public Contrat findById(UUID id) {
        return findByIdOrThrow(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Contrat> findByCriteria(UUID clientId, UUID vehiculeId, EtatContrat etat) {
        return contratRepository.findByCriteria(clientId, vehiculeId, etat);
    }

    // === Helpers ===

    private Contrat findByIdOrThrow(UUID id) {
        return contratRepository.findById(id)
            .orElseThrow(() -> new ContratNotFoundException(
                String.format("Contrat %s introuvable", id)
            ));
    }
}
