package com.BFBManagement.presentation.contrats.internal;

import java.util.UUID;

/**
 * DTO de réponse pour l'événement "véhicule en panne".
 */
public record VehicleMarkedDownResponse(UUID vehiculeId, int contractsCanceled) {}
