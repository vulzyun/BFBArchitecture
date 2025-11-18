package com.BFBManagement.business.contrats.model;

import java.time.LocalDate;
import java.util.Set;

/**
 * Règles métier pures pour les contrats (sans dépendances externes).
 * Ces règles sont le cœur invariant du domaine.
 */
public final class Rules {

    private Rules() {
        // Classe utilitaire, pas d'instanciation
    }

    /**
     * Vérifie que deux intervalles de dates ne se chevauchent PAS.
     * Convention : intervalles fermés [début, fin] (les bornes sont incluses).
     * 
     * @param a1 début de l'intervalle A
     * @param a2 fin de l'intervalle A
     * @param b1 début de l'intervalle B
     * @param b2 fin de l'intervalle B
     * @return true si aucun chevauchement, false sinon
     */
    public static boolean noOverlap(LocalDate a1, LocalDate a2, LocalDate b1, LocalDate b2) {
        // Pas de chevauchement si A est complètement avant B OU B est complètement avant A
        // A avant B : a2 < b1
        // B avant A : b2 < a1
        return a2.isBefore(b1) || b2.isBefore(a1);
    }

    /**
     * Vérifie si une transition d'état est autorisée selon les règles métier.
     * 
     * Transitions autorisées :
     * - EN_ATTENTE → EN_COURS, ANNULE
     * - EN_COURS → TERMINE, EN_RETARD
     * - EN_RETARD → TERMINE
     * - Même état → même état (idempotence)
     * 
     * @param from état de départ
     * @param to état d'arrivée
     * @return true si la transition est autorisée, false sinon
     */
    public static boolean transitionAllowed(EtatContrat from, EtatContrat to) {
        // Idempotence : même état autorisé
        if (from == to) {
            return true;
        }
        
        // Transitions autorisées selon la matrice métier
        return switch (from) {
            case EN_ATTENTE -> Set.of(EtatContrat.EN_COURS, EtatContrat.ANNULE).contains(to);
            case EN_COURS -> Set.of(EtatContrat.TERMINE, EtatContrat.EN_RETARD).contains(to);
            case EN_RETARD -> to == EtatContrat.TERMINE;
            case TERMINE, ANNULE -> false; // États terminaux, pas de sortie
        };
    }
}
