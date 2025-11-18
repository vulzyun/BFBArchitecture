package com.bfb.business.contract.model;

import java.util.Map;
import java.util.Set;

/**
 * Business rules for contract state transitions.
 */
public class Rules {

    private static final Map<ContractStatus, Set<ContractStatus>> ALLOWED_TRANSITIONS = Map.of(
        ContractStatus.PENDING, Set.of(ContractStatus.IN_PROGRESS, ContractStatus.CANCELLED),
        ContractStatus.IN_PROGRESS, Set.of(ContractStatus.COMPLETED, ContractStatus.LATE),
        ContractStatus.LATE, Set.of(ContractStatus.COMPLETED)
    );

    /**
     * Checks if a state transition is allowed.
     */
    public static boolean isTransitionAllowed(ContractStatus from, ContractStatus to) {
        Set<ContractStatus> allowedTargets = ALLOWED_TRANSITIONS.get(from);
        return allowedTargets != null && allowedTargets.contains(to);
    }

    private Rules() {
        // Utility class
    }
}
