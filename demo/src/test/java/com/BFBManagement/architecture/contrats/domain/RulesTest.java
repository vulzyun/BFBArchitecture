package com.BFBManagement.architecture.contrats.domain;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires des règles métier pures (sans dépendances externes).
 * TDD RED : ces tests doivent échouer tant que Rules n'existe pas.
 */
class RulesTest {

    // ==================== Tests noOverlap ====================
    
    @Test
    void noOverlap_returnsTrue_whenIntervalsDoNotIntersect() {
        // Cas 1 : A complètement avant B
        LocalDate a1 = LocalDate.of(2025, 12, 1);
        LocalDate a2 = LocalDate.of(2025, 12, 10);
        LocalDate b1 = LocalDate.of(2025, 12, 15);
        LocalDate b2 = LocalDate.of(2025, 12, 20);
        
        assertTrue(Rules.noOverlap(a1, a2, b1, b2), "A avant B → pas de chevauchement");
        assertTrue(Rules.noOverlap(b1, b2, a1, a2), "B avant A → pas de chevauchement");
    }
    
    @Test
    void noOverlap_returnsFalse_whenIntervalsTouch() {
        // A.fin = B.début (contact exact)
        LocalDate a1 = LocalDate.of(2025, 12, 1);
        LocalDate a2 = LocalDate.of(2025, 12, 10);
        LocalDate b1 = LocalDate.of(2025, 12, 10);
        LocalDate b2 = LocalDate.of(2025, 12, 20);
        
        assertFalse(Rules.noOverlap(a1, a2, b1, b2), "A.fin = B.début → chevauchement (jour commun)");
    }
    
    @Test
    void noOverlap_returnsFalse_whenIntervalsPartiallyOverlap() {
        // Chevauchement partiel
        LocalDate a1 = LocalDate.of(2025, 12, 1);
        LocalDate a2 = LocalDate.of(2025, 12, 10);
        LocalDate b1 = LocalDate.of(2025, 12, 5);
        LocalDate b2 = LocalDate.of(2025, 12, 15);
        
        assertFalse(Rules.noOverlap(a1, a2, b1, b2), "Chevauchement partiel");
        assertFalse(Rules.noOverlap(b1, b2, a1, a2), "Chevauchement partiel (inversé)");
    }
    
    @Test
    void noOverlap_returnsFalse_whenOneIntervalContainsTheOther() {
        // B complètement inclus dans A
        LocalDate a1 = LocalDate.of(2025, 12, 1);
        LocalDate a2 = LocalDate.of(2025, 12, 20);
        LocalDate b1 = LocalDate.of(2025, 12, 5);
        LocalDate b2 = LocalDate.of(2025, 12, 10);
        
        assertFalse(Rules.noOverlap(a1, a2, b1, b2), "B inclus dans A → chevauchement");
        assertFalse(Rules.noOverlap(b1, b2, a1, a2), "A contient B → chevauchement");
    }
    
    @Test
    void noOverlap_returnsFalse_whenIntervalsAreIdentical() {
        LocalDate a1 = LocalDate.of(2025, 12, 1);
        LocalDate a2 = LocalDate.of(2025, 12, 10);
        
        assertFalse(Rules.noOverlap(a1, a2, a1, a2), "Intervalles identiques → chevauchement");
    }

    // ==================== Tests transitionAllowed ====================
    
    @Test
    void transitionAllowed_all_valid_cases() {
        // EN_ATTENTE → EN_COURS
        assertTrue(Rules.transitionAllowed(EtatContrat.EN_ATTENTE, EtatContrat.EN_COURS), 
            "EN_ATTENTE → EN_COURS autorisé");
        
        // EN_ATTENTE → ANNULE
        assertTrue(Rules.transitionAllowed(EtatContrat.EN_ATTENTE, EtatContrat.ANNULE), 
            "EN_ATTENTE → ANNULE autorisé");
        
        // EN_COURS → TERMINE
        assertTrue(Rules.transitionAllowed(EtatContrat.EN_COURS, EtatContrat.TERMINE), 
            "EN_COURS → TERMINE autorisé");
        
        // EN_COURS → EN_RETARD
        assertTrue(Rules.transitionAllowed(EtatContrat.EN_COURS, EtatContrat.EN_RETARD), 
            "EN_COURS → EN_RETARD autorisé");
    }
    
    @Test
    void transitionAllowed_throws_on_invalid_cases() {
        // TERMINE → toute autre état : interdit
        assertFalse(Rules.transitionAllowed(EtatContrat.TERMINE, EtatContrat.EN_COURS), 
            "TERMINE → EN_COURS interdit");
        assertFalse(Rules.transitionAllowed(EtatContrat.TERMINE, EtatContrat.EN_ATTENTE), 
            "TERMINE → EN_ATTENTE interdit");
        
        // ANNULE → toute autre état : interdit
        assertFalse(Rules.transitionAllowed(EtatContrat.ANNULE, EtatContrat.EN_COURS), 
            "ANNULE → EN_COURS interdit");
        
        // EN_RETARD → EN_ATTENTE : interdit
        assertFalse(Rules.transitionAllowed(EtatContrat.EN_RETARD, EtatContrat.EN_ATTENTE), 
            "EN_RETARD → EN_ATTENTE interdit");
        
        // EN_COURS → EN_ATTENTE : interdit (pas de retour arrière)
        assertFalse(Rules.transitionAllowed(EtatContrat.EN_COURS, EtatContrat.EN_ATTENTE), 
            "EN_COURS → EN_ATTENTE interdit");
        
        // EN_ATTENTE → EN_RETARD : interdit (on ne peut pas être en retard sans avoir commencé)
        assertFalse(Rules.transitionAllowed(EtatContrat.EN_ATTENTE, EtatContrat.EN_RETARD), 
            "EN_ATTENTE → EN_RETARD interdit");
        
        // EN_RETARD → TERMINE : autorisé (on peut terminer un contrat en retard)
        assertTrue(Rules.transitionAllowed(EtatContrat.EN_RETARD, EtatContrat.TERMINE), 
            "EN_RETARD → TERMINE autorisé");
    }
    
    @Test
    void transitionAllowed_sameState_isAllowed() {
        // Transition vers le même état = idempotent, autorisé
        assertTrue(Rules.transitionAllowed(EtatContrat.EN_ATTENTE, EtatContrat.EN_ATTENTE));
        assertTrue(Rules.transitionAllowed(EtatContrat.EN_COURS, EtatContrat.EN_COURS));
    }
}
