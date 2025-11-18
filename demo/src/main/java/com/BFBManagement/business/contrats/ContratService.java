package com.BFBManagement.business.contrats;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.BFBManagement.business.contrats.exceptions.ClientUnknownException;
import com.BFBManagement.business.contrats.exceptions.ContratNotFoundException;
import com.BFBManagement.business.contrats.exceptions.OverlapException;
import com.BFBManagement.business.contrats.exceptions.TransitionNotAllowedException;
import com.BFBManagement.business.contrats.exceptions.ValidationException;
import com.BFBManagement.business.contrats.exceptions.VehicleUnavailableException;
import com.BFBManagement.business.contrats.model.Contrat;
import com.BFBManagement.business.contrats.model.EtatContrat;
import com.BFBManagement.business.contrats.model.Rules;
import com.BFBManagement.infrastructures.bdd.contrats.ContratBddService;
import com.BFBManagement.infrastructures.external.ClientExistenceService;
import com.BFBManagement.infrastructures.external.VehicleStatusService;

/**
 * Service métier pour la gestion des contrats de location.
 * Implémente toutes les règles métier et validations.
 */
@Service
@Transactional
public class ContratService {

    private final ContratBddService contratBddService;
    private final VehicleStatusService vehicleStatusService;
    private final ClientExistenceService clientExistenceService;

    public ContratService(
            ContratBddService contratBddService,
            VehicleStatusService vehicleStatusService,
            ClientExistenceService clientExistenceService) {
        this.contratBddService = contratBddService;
        this.vehicleStatusService = vehicleStatusService;
        this.clientExistenceService = clientExistenceService;
    }

    public Contrat create(UUID clientId, UUID vehiculeId, LocalDate dateDebut, LocalDate dateFin) {
        // 1. Valider les dates
        if (!dateDebut.isBefore(dateFin)) {
            throw new ValidationException(
                "La dateDebut doit être strictement antérieure à la dateFin"
            );
        }

        // 2. Vérifier que le client existe
        if (!clientExistenceService.existsById(clientId)) {
            throw new ClientUnknownException(
                String.format("Client %s introuvable", clientId)
            );
        }

        // 3. Vérifier que le véhicule n'est pas EN_PANNE
        var vehiculeStatus = vehicleStatusService.getStatus(vehiculeId);
        if (vehiculeStatus == com.BFBManagement.business.vehicules.model.EtatVehicule.EN_PANNE) {
            throw new VehicleUnavailableException(
                String.format("Le véhicule %s est EN_PANNE et ne peut être loué", vehiculeId)
            );
        }

        // 4. Vérifier les chevauchements
        List<Contrat> overlappingContrats = contratBddService.findOverlappingContrats(
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
        return contratBddService.save(contrat);
    }

    public Contrat start(UUID contratId) {
        Contrat contrat = findByIdOrThrow(contratId);
        
        if (!Rules.transitionAllowed(contrat.getEtat(), EtatContrat.EN_COURS)) {
            throw new TransitionNotAllowedException(
                String.format("Impossible de démarrer un contrat en état %s", contrat.getEtat())
            );
        }
        
        contrat.start();
        return contratBddService.save(contrat);
    }

    public Contrat terminate(UUID contratId) {
        Contrat contrat = findByIdOrThrow(contratId);
        
        if (!Rules.transitionAllowed(contrat.getEtat(), EtatContrat.TERMINE)) {
            throw new TransitionNotAllowedException(
                String.format("Impossible de terminer un contrat en état %s", contrat.getEtat())
            );
        }
        
        contrat.terminate();
        return contratBddService.save(contrat);
    }

    public Contrat cancel(UUID contratId) {
        Contrat contrat = findByIdOrThrow(contratId);
        
        if (!Rules.transitionAllowed(contrat.getEtat(), EtatContrat.ANNULE)) {
            throw new TransitionNotAllowedException(
                String.format("Impossible d'annuler un contrat en état %s", contrat.getEtat())
            );
        }
        
        contrat.cancel();
        return contratBddService.save(contrat);
    }

    public int markLateIfOverdue() {
        LocalDate today = LocalDate.now();
        List<Contrat> contratsEnCours = contratBddService.findByEtat(EtatContrat.EN_COURS);
        
        int count = 0;
        for (Contrat contrat : contratsEnCours) {
            if (contrat.getDateFin().isBefore(today)) {
                contrat.markLate();
                contratBddService.save(contrat);
                count++;
            }
        }
        
        return count;
    }

    public int cancelPendingContractsForVehicle(UUID vehiculeId) {
        List<Contrat> pendingContrats = contratBddService.findByVehiculeIdAndEtat(
            vehiculeId, 
            EtatContrat.EN_ATTENTE
        );
        
        int count = 0;
        for (Contrat contrat : pendingContrats) {
            contrat.cancel();
            contratBddService.save(contrat);
            count++;
        }
        
        return count;
    }

    @Transactional(readOnly = true)
    public Contrat findById(UUID id) {
        return findByIdOrThrow(id);
    }

    @Transactional(readOnly = true)
    public List<Contrat> findByCriteria(UUID clientId, UUID vehiculeId, EtatContrat etat) {
        return contratBddService.findByCriteria(clientId, vehiculeId, etat);
    }

    // === Helpers ===

    private Contrat findByIdOrThrow(UUID id) {
        return contratBddService.findById(id)
            .orElseThrow(() -> new ContratNotFoundException(
                String.format("Contrat %s introuvable", id)
            ));
    }
}
