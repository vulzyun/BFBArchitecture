package com.BFBManagement.presentation.contrats;

import com.BFBManagement.infrastructure.contrats.domain.Contrat;
import com.BFBManagement.infrastructure.contrats.domain.ContratRepository;
import com.BFBManagement.infrastructure.contrats.domain.EtatContrat;
import com.BFBManagement.business.contrats.ports.ClientExistencePort;
import com.BFBManagement.business.contrats.ports.VehicleStatusPort;
import com.BFBManagement.business.vehicules.EtatVehicule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d'intégration pour le ContratController.
 * Teste les endpoints REST avec un contexte Spring complet.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ContratControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ContratRepository contratRepository;

    // Note: @MockBean is deprecated in Spring Boot 3.4+ but still functional
    // Will be replaced with the official replacement annotation when available
    @MockBean
    private VehicleStatusPort vehicleStatusPort;

    @MockBean
    private ClientExistencePort clientExistencePort;

    @BeforeEach
    void setUp() {
        contratRepository.deleteAll();
        
        // Par défaut, on mock les ports externes pour qu'ils renvoient des valeurs OK
        when(clientExistencePort.existsById(any())).thenReturn(true);
        when(vehicleStatusPort.getStatus(any())).thenReturn(EtatVehicule.DISPONIBLE);
    }

    @Test
    void vehicle_marked_down_endpoint_triggers_cancel_pending() throws Exception {
        // Given: 3 contrats pour le même véhicule
        UUID vehiculeId = UUID.randomUUID();
        
        Contrat contratEnAttente = new Contrat(
            UUID.randomUUID(),
            vehiculeId,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(5),
            EtatContrat.EN_ATTENTE
        );
        
        Contrat contratEnCours = new Contrat(
            UUID.randomUUID(),
            vehiculeId,
            LocalDate.now().minusDays(2),
            LocalDate.now().plusDays(3),
            EtatContrat.EN_COURS
        );
        
        Contrat contratEnRetard = new Contrat(
            UUID.randomUUID(),
            vehiculeId,
            LocalDate.now().minusDays(10),
            LocalDate.now().minusDays(1),
            EtatContrat.EN_RETARD
        );
        
        contratRepository.save(contratEnAttente);
        contratRepository.save(contratEnCours);
        contratRepository.save(contratEnRetard);
        
        String requestBody = String.format("{\"vehiculeId\":\"%s\"}", vehiculeId);
        
        // When: POST /internal/events/vehicules/marked-down
        mockMvc.perform(post("/internal/events/vehicules/marked-down")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isAccepted());
        
        // Then: Vérifie que seul le contrat EN_ATTENTE est ANNULE
        mockMvc.perform(get("/api/contrats")
                .param("vehiculeId", vehiculeId.toString())
                .param("etat", "ANNULE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        
        mockMvc.perform(get("/api/contrats")
                .param("vehiculeId", vehiculeId.toString())
                .param("etat", "EN_COURS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        
        mockMvc.perform(get("/api/contrats")
                .param("vehiculeId", vehiculeId.toString())
                .param("etat", "EN_RETARD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void create_contract_validates_dto() throws Exception {
        // When/Then: requête avec champs manquants/invalides doit renvoyer 400
        String invalidDto = "{}"; // Tous les champs obligatoires manquent
        
        mockMvc.perform(post("/api/contrats")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidDto))
                .andExpect(status().isBadRequest());
        
        // Test avec dates invalides (dateFin avant dateDebut)
        String invalidDatesDto = String.format("""
            {
                "clientId": "%s",
                "vehiculeId": "%s",
                "dateDebut": "2025-12-10",
                "dateFin": "2025-12-01"
            }
            """, UUID.randomUUID(), UUID.randomUUID());
        
        mockMvc.perform(post("/api/contrats")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidDatesDto))
                .andExpect(status().isBadRequest());
    }
}
