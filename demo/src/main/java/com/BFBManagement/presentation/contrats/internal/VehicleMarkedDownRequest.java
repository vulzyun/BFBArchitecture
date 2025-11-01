package com.BFBManagement.presentation.contrats.internal;

import java.util.UUID;

/**
 * DTO de requête pour l'événement "véhicule en panne".
 */
public record VehicleMarkedDownRequest(UUID vehiculeId) {}
