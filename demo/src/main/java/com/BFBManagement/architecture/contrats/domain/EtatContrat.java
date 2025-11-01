package com.BFBManagement.architecture.contrats.domain;

/**
 * États possibles d'un contrat de location.
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
