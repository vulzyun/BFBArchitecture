package com.BFBManagement.business.contrats;

import com.BFBManagement.architecture.contrats.domain.*;
import com.BFBManagement.business.contrats.exceptions.*;
import com.BFBManagement.business.contrats.ports.ClientExistencePort;
import com.BFBManagement.business.contrats.ports.VehicleStatusPort;
import com.BFBManagement.business.vehicules.EtatVehicule;
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
public class ContratService {

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

    /**
     * Crée un nouveau contrat avec toutes les validations métier.
     */
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
        Contrat contrat = new Contrat(clientId, vehiculeId, dateDebut, dateFin, EtatContrat.EN_ATTENTE);
        return contratRepository.save(contrat);
    }

    /**
     * Démarre un contrat (EN_ATTENTE → EN_COURS).
     */
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

    /**
     * Termine un contrat (EN_COURS ou EN_RETARD → TERMINE).
     */
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

    /**
     * Annule un contrat (EN_ATTENTE → ANNULE).
     */
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

    /**
     * Marque automatiquement en retard tous les contrats EN_COURS dont la dateFin est dépassée.
     * Job à exécuter périodiquement.
     */
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

    /**
     * Annule tous les contrats EN_ATTENTE d'un véhicule.
     * Utilisé lorsque le véhicule est marqué en panne.
     */
    public int cancelPendingContractsForVehicle(UUID vehiculeId) {
        List<Contrat> pendingContrats = contratRepository.findByVehiculeIdAndEtat(
            vehiculeId, 
            EtatContrat.EN_ATTENTE
        );
        
        int count = 0;
        for (Contrat contrat : pendingContrats) {
            if (contrat.getEtat() == EtatContrat.EN_ATTENTE) {
                contrat.cancel();
                contratRepository.save(contrat);
                count++;
            }
        }
        
        return count;
    }

    /**
     * Job combiné : marque en retard les contrats EN_COURS dépassés 
     * et annule les contrats EN_ATTENTE bloqués par ces retards.
     * 
     * Logique :
     * 1. Passe EN_COURS → EN_RETARD si dateFin < today
     * 2. Pour chaque contrat EN_RETARD, annule les EN_ATTENTE du même véhicule 
     *    si dateDebut <= today (considérés comme bloqués)
     */
    public int markLateAndCancelBlocked() {
        LocalDate today = LocalDate.now();
        int totalModified = 0;
        
        // Étape 1 : Marquer en retard les contrats EN_COURS dépassés
        List<Contrat> contratsEnCours = contratRepository.findByEtat(EtatContrat.EN_COURS);
        for (Contrat contrat : contratsEnCours) {
            if (contrat.getDateFin().isBefore(today)) {
                contrat.markLate();
                contratRepository.save(contrat);
                totalModified++;
                
                // Étape 2 : Annuler les EN_ATTENTE bloqués sur le même véhicule
                List<Contrat> awaitingForVehicle = contratRepository.findByVehiculeIdAndEtat(
                    contrat.getVehiculeId(), 
                    EtatContrat.EN_ATTENTE
                );
                
                for (Contrat awaitingContrat : awaitingForVehicle) {
                    // Considéré comme bloqué si dateDebut <= today
                    if (!awaitingContrat.getDateDebut().isAfter(today)) {
                        awaitingContrat.cancel();
                        contratRepository.save(awaitingContrat);
                        totalModified++;
                    }
                }
            }
        }
        
        return totalModified;
    }

    /**
     * Récupère un contrat par ID.
     */
    @Transactional(readOnly = true)
    public Contrat findById(UUID id) {
        return findByIdOrThrow(id);
    }

    /**
     * Recherche des contrats selon des critères optionnels.
     */
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
