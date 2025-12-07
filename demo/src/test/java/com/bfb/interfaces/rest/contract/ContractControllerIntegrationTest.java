package com.bfb.interfaces.rest.contract;

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
 * Each test creates its own unique test data to ensure complete isolation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ContractControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Helper method to create a unique client for testing.
     * Each client has unique identifiers to prevent data conflicts.
     */
    private UUID createTestClient() throws Exception {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        String clientJson = String.format(
            "{\"firstName\":\"TestFirst\",\"lastName\":\"TestLast\",\"address\":\"123 Test Street\",\"licenseNumber\":\"LIC-%s\",\"birthDate\":\"1990-05-15\"}",
            uniqueSuffix
        );
        
        String response = mockMvc.perform(post("/api/v1/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(clientJson))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        return UUID.fromString(objectMapper.readTree(response).get("id").asText());
    }

    /**
     * Helper method to create a unique vehicle for testing.
     * Each vehicle has a unique registration plate to prevent conflicts.
     */
    private UUID createTestVehicle() throws Exception {
        long timestamp = System.currentTimeMillis();
        String uniquePlate = "TEST-" + timestamp;
        
        String vehicleJson = String.format(
            "{\"brand\":\"TestBrand\",\"model\":\"TestModel\",\"motorization\":\"Petrol\",\"color\":\"Blue\",\"registrationPlate\":\"%s\",\"purchaseDate\":\"2020-01-01\"}",
            uniquePlate
        );
        
        String response = mockMvc.perform(post("/api/v1/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(vehicleJson))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        return UUID.fromString(objectMapper.readTree(response).get("id").asText());
    }

    @Test
    void createContract_Success() throws Exception {
        // Given - create unique test data
        UUID clientId = createTestClient();
        UUID vehicleId = createTestVehicle();
        
        CreateContractRequest request = new CreateContractRequest(
            clientId,
            vehicleId,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(8)
        );

        // When & Then
        mockMvc.perform(post("/api/v1/contracts")
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
        mockMvc.perform(post("/api/v1/contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Parameter validation failed"));
    }

    @Test
    void createContract_MissingFields_ReturnsBadRequest() throws Exception {
        // Given - missing required fields
        String invalidJson = "{}";

        // When & Then
        mockMvc.perform(post("/api/v1/contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createContract_OverlappingDates_ReturnsConflict() throws Exception {
        // Given - create unique test data for this test
        UUID clientId = createTestClient();
        UUID vehicleId = createTestVehicle();
        
        // Create first contract
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(8);
        
        CreateContractRequest request1 = new CreateContractRequest(
            clientId,
            vehicleId,
            startDate,
            endDate
        );
        
        mockMvc.perform(post("/api/v1/contracts")
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
        mockMvc.perform(post("/api/v1/contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.title").value("Business conflict"))
            .andExpect(jsonPath("$.detail").value(containsString("already booked")));
    }

    @Test
    void getContractById_Success() throws Exception {
        // Given - create unique test data and a contract
        UUID clientId = createTestClient();
        UUID vehicleId = createTestVehicle();
        
        CreateContractRequest request = new CreateContractRequest(
            clientId,
            vehicleId,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(8)
        );
        
        String createResponse = mockMvc.perform(post("/api/v1/contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        String contractId = objectMapper.readTree(createResponse).get("id").asText();

        // When & Then
        mockMvc.perform(get("/api/v1/contracts/{id}", contractId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(contractId))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getContractById_NotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(get("/api/v1/contracts/{id}", nonExistentId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    @Test
    void startContract_Success() throws Exception {
        // Given - create unique test data and a pending contract
        UUID clientId = createTestClient();
        UUID vehicleId = createTestVehicle();
        
        CreateContractRequest request = new CreateContractRequest(
            clientId,
            vehicleId,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(8)
        );
        
        String createResponse = mockMvc.perform(post("/api/v1/contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        String contractId = objectMapper.readTree(createResponse).get("id").asText();

        // When & Then
        mockMvc.perform(patch("/api/v1/contracts/{id}/start", contractId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void startContract_AlreadyStarted_ReturnsUnprocessableEntity() throws Exception {
        // Given - create unique test data, create and start a contract
        UUID clientId = createTestClient();
        UUID vehicleId = createTestVehicle();
        
        CreateContractRequest request = new CreateContractRequest(
            clientId,
            vehicleId,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(8)
        );
        
        String createResponse = mockMvc.perform(post("/api/v1/contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        String contractId = objectMapper.readTree(createResponse).get("id").asText();
        
        mockMvc.perform(patch("/api/v1/contracts/{id}/start", contractId))
            .andExpect(status().isOk());

        // When - try to start again
        // Then
        mockMvc.perform(patch("/api/v1/contracts/{id}/start", contractId))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.title").value("State transition not allowed"));
    }

    @Test
    void terminateContract_Success() throws Exception {
        // Given - create unique test data, create and start a contract
        UUID clientId = createTestClient();
        UUID vehicleId = createTestVehicle();
        
        CreateContractRequest request = new CreateContractRequest(
            clientId,
            vehicleId,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(8)
        );
        
        String createResponse = mockMvc.perform(post("/api/v1/contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        String contractId = objectMapper.readTree(createResponse).get("id").asText();
        
        mockMvc.perform(patch("/api/v1/contracts/{id}/start", contractId))
            .andExpect(status().isOk());

        // When & Then
        mockMvc.perform(patch("/api/v1/contracts/{id}/terminate", contractId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void cancelContract_Success() throws Exception {
        // Given - create unique test data and a pending contract
        UUID clientId = createTestClient();
        UUID vehicleId = createTestVehicle();
        
        CreateContractRequest request = new CreateContractRequest(
            clientId,
            vehicleId,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(8)
        );
        
        String createResponse = mockMvc.perform(post("/api/v1/contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        String contractId = objectMapper.readTree(createResponse).get("id").asText();

        // When & Then
        mockMvc.perform(patch("/api/v1/contracts/{id}/cancel", contractId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void searchContracts_WithoutFilters_ReturnsAllContracts() throws Exception {
        // Given - create unique test data for multiple contracts
        UUID clientId = createTestClient();
        UUID vehicleId1 = createTestVehicle();
        UUID vehicleId2 = createTestVehicle();
        
        CreateContractRequest request1 = new CreateContractRequest(
            clientId,
            vehicleId1,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(8)
        );
        
        CreateContractRequest request2 = new CreateContractRequest(
            clientId,
            vehicleId2,
            LocalDate.now().plusDays(10),
            LocalDate.now().plusDays(15)
        );
        
        mockMvc.perform(post("/api/v1/contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
            .andExpect(status().isCreated());
        
        mockMvc.perform(post("/api/v1/contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
            .andExpect(status().isCreated());

        // When & Then
        mockMvc.perform(get("/api/v1/contracts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(2))))
            .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(2)))
            .andExpect(jsonPath("$.pageable").exists());
    }

    @Test
    void searchContracts_WithStatusFilter() throws Exception {
        // Given - create unique test data for contracts with different statuses
        UUID clientId = createTestClient();
        UUID vehicleId1 = createTestVehicle();
        UUID vehicleId2 = createTestVehicle();
        
        CreateContractRequest request1 = new CreateContractRequest(
            clientId,
            vehicleId1,
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
        mockMvc.perform(post("/api/v1/contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
            .andExpect(status().isCreated());
        
        // Create and start second contract
        String createResponse = mockMvc.perform(post("/api/v1/contracts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        
        String contractId = objectMapper.readTree(createResponse).get("id").asText();
        
        mockMvc.perform(patch("/api/v1/contracts/{id}/start", contractId))
            .andExpect(status().isOk());

        // When & Then - search for pending contracts
        mockMvc.perform(get("/api/v1/contracts")
                .param("status", "PENDING"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[*].status", everyItem(is("PENDING"))))
            .andExpect(jsonPath("$.pageable").exists());
    }

    @Test
    void markLateJob_Success() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/contracts/jobs/mark-late"))
            .andExpect(status().isAccepted())
            .andExpect(jsonPath("$.contractsMarkedLate").isNumber());
    }
}

