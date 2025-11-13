package com.BFBManagement.domain.contrats;

/**
 * États possibles d'un contrat de location.
 * 
 * Machine à états avec transitions contrôlées :
 * - EN_ATTENTE → EN_COURS (start)
 * - EN_ATTENTE → ANNULE (cancel)
 * - EN_COURS → TERMINE (terminate)
 * - EN_COURS → EN_RETARD (markLate - automatique)
 * - EN_RETARD → TERMINE (terminate)
 */
public enum EtatContrat {
    /**
     * Contrat créé, en attente du début effectif.
     */
    EN_ATTENTE,
    
    /**
     * Contrat actuellement en cours.
     */
    EN_COURS,
    
    /**
     * Contrat terminé normalement.
     */
    TERMINE,
    
    /**
     * Contrat en retard (dateFin dépassée mais pas encore terminé).
     */
    EN_RETARD,
    
    /**
     * Contrat annulé avant d'avoir démarré.
     */
    ANNULE
}
