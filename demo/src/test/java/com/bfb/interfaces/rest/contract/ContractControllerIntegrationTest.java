package com.bfb.interfaces.rest.contract;

import com.bfb.business.contract.model.ContractStatus;
import com.bfb.interfaces.rest.contract.dto.CreateContractRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ContractController.
 * Tests the full stack with Spring context and H2 database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ContractControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID clientId;
    private UUID vehicleId;

    @org.junit.jupiter.api.BeforeEach
    void setUp() throws Exception {
        // Create a test client
        String clientResponse = mockMvc.perform(post("/api/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test Client\",\"email\":\"test" + System.nanoTime() + "@example.com\"}"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        clientId = UUID.fromString(objectMapper.readTree(clientResponse).get("id").asText());

        // Create a test vehicle
        String vehicleResponse = mockMvc.perform(post("/api/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"brand\":\"Toyota\",\"model\":\"Corolla\"}"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        vehicleId = UUID.fromString(objectMapper.readTree(vehicleResponse).get("id").asText());
    }

    @Test
    void createContract_Success() throws Exception {
        // Given
        CreateContractRequest request = new CreateContractRequest(
            clientId,
            vehicleId,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(8)
        );

        // When & Then
        mockMvc.perform(post("/api/contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.clientId").value(request.clientId().toString()))
            .andExpect(jsonPath("$.vehicleId").value(request.vehicleId().toString()))
            .andExpect(jsonPath("$.startDate").value(request.startDate().toString()))
            .andExpect(jsonPath("$.endDate").value(request.endDate().toString()))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createContract_InvalidDates_ReturnsBadRequest() throws Exception {
        // Given - end date before start date
        CreateContractRequest request = new CreateContractRequest(
            UUID.randomUUID(),
            UUID.randomUUID(),
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(5)
        );

        // When & Then
        mockMvc.perform(post("/api/contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    void createContract_MissingFields_ReturnsBadRequest() throws Exception {
        // Given - missing required fields
        String invalidJson = "{}";

        // When & Then
        mockMvc.perform(post("/api/contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createContract_OverlappingDates_ReturnsConflict() throws Exception {
        // Given - create first contract
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(8);
        
        CreateContractRequest request1 = new CreateContractRequest(
            clientId,
            vehicleId,
            startDate,
            endDate
        );
        
        mockMvc.perform(post("/api/contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
            .andExpect(status().isCreated());

        // When - create overlapping contract
        CreateContractRequest request2 = new CreateContractRequest(
            clientId,
            vehicleId,
            startDate.plusDays(2),
            endDate.plusDays(2)
        );

        // Then
        mockMvc.perform(post("/api/contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.title").value("Business conflict"))
            .andExpect(jsonPath("$.detail").value(containsString("Overlap detected")));
    }

    @Test
    void getContractById_Success() throws Exception {
        // Given - create a contract first
        CreateContractRequest request = new CreateContractRequest(
            clientId,
            vehicleId,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(8)
        );
        
        String createResponse = mockMvc.perform(post("/api/contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        String contractId = objectMapper.readTree(createResponse).get("id").asText();

        // When & Then
        mockMvc.perform(get("/api/contracts/{id}", contractId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(contractId))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getContractById_NotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(get("/api/contracts/{id}", nonExistentId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    @Test
    void startContract_Success() throws Exception {
        // Given - create a pending contract
        CreateContractRequest request = new CreateContractRequest(
            clientId,
            vehicleId,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(8)
        );
        
        String createResponse = mockMvc.perform(post("/api/contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        String contractId = objectMapper.readTree(createResponse).get("id").asText();

        // When & Then
        mockMvc.perform(patch("/api/contracts/{id}/start", contractId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void startContract_AlreadyStarted_ReturnsUnprocessableEntity() throws Exception {
        // Given - create and start a contract
        CreateContractRequest request = new CreateContractRequest(
            clientId,
            vehicleId,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(8)
        );
        
        String createResponse = mockMvc.perform(post("/api/contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        String contractId = objectMapper.readTree(createResponse).get("id").asText();
        
        mockMvc.perform(patch("/api/contracts/{id}/start", contractId))
            .andExpect(status().isOk());

        // When - try to start again
        // Then
        mockMvc.perform(patch("/api/contracts/{id}/start", contractId))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.title").value("State transition not allowed"));
    }

    @Test
    void terminateContract_Success() throws Exception {
        // Given - create and start a contract
        CreateContractRequest request = new CreateContractRequest(
            clientId,
            vehicleId,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(8)
        );
        
        String createResponse = mockMvc.perform(post("/api/contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        String contractId = objectMapper.readTree(createResponse).get("id").asText();
        
        mockMvc.perform(patch("/api/contracts/{id}/start", contractId))
            .andExpect(status().isOk());

        // When & Then
        mockMvc.perform(patch("/api/contracts/{id}/terminate", contractId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void cancelContract_Success() throws Exception {
        // Given - create a pending contract
        CreateContractRequest request = new CreateContractRequest(
            clientId,
            vehicleId,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(8)
        );
        
        String createResponse = mockMvc.perform(post("/api/contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        String contractId = objectMapper.readTree(createResponse).get("id").asText();

        // When & Then
        mockMvc.perform(patch("/api/contracts/{id}/cancel", contractId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void searchContracts_WithoutFilters_ReturnsAllContracts() throws Exception {
        // Given - create multiple contracts
        CreateContractRequest request1 = new CreateContractRequest(
            clientId,
            vehicleId,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(8)
        );
        
        // Create another vehicle for second contract
        String vehicle2Response = mockMvc.perform(post("/api/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"brand\":\"Honda\",\"model\":\"Civic\"}"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        UUID vehicleId2 = UUID.fromString(objectMapper.readTree(vehicle2Response).get("id").asText());
        
        CreateContractRequest request2 = new CreateContractRequest(
            clientId,
            vehicleId2,
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(15)
        );
        
        mockMvc.perform(post("/api/contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
            .andExpect(status().isCreated());
        
        mockMvc.perform(post("/api/contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
            .andExpect(status().isCreated());

        // When & Then
        mockMvc.perform(get("/api/contracts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    void searchContracts_WithStatusFilter() throws Exception {
        // Given - create contracts with different statuses
        // Create another vehicle for second contract
        String vehicle2Response = mockMvc.perform(post("/api/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"brand\":\"Honda\",\"model\":\"Accord\"}"))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        UUID vehicleId2 = UUID.fromString(objectMapper.readTree(vehicle2Response).get("id").asText());
        
        CreateContractRequest request1 = new CreateContractRequest(
            clientId,
            vehicleId,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(8)
        );
        CreateContractRequest request2 = new CreateContractRequest(
            clientId,
            vehicleId2,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(8)
        );
        
        // Create first contract
        mockMvc.perform(post("/api/contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
            .andExpect(status().isCreated());
        
        // Create and start second contract
        String createResponse = mockMvc.perform(post("/api/contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        String contractId = objectMapper.readTree(createResponse).get("id").asText();
        
        mockMvc.perform(patch("/api/contracts/{id}/start", contractId))
            .andExpect(status().isOk());

        // When & Then - search for pending contracts
        mockMvc.perform(get("/api/contracts")
                .param("status", "PENDING"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[*].status", everyItem(is("PENDING"))));
    }

    @Test
    void markLateJob_Success() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/contracts/jobs/mark-late"))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.contractsMarkedLate").isNumber());
    }
}
