package com.bfb.business.contract.model;

import com.bfb.business.contract.exception.TransitionNotAllowedException;

import java.util.EnumSet;
import java.util.Set;

/**
 * Contract status enumeration with embedded state transition logic.
 * Uses the State Pattern to enforce valid state transitions.
 */
public enum ContractStatus {
    
    PENDING {
        @Override
        public Set<ContractStatus> getAllowedTransitions() {
            return EnumSet.of(IN_PROGRESS, CANCELLED);
        }
    },
    
    IN_PROGRESS {
        @Override
        public Set<ContractStatus> getAllowedTransitions() {
            return EnumSet.of(LATE, COMPLETED);
        }
    },
    
    LATE {
        @Override
        public Set<ContractStatus> getAllowedTransitions() {
            return EnumSet.of(COMPLETED);
        }
    },
    
    COMPLETED {
        @Override
        public Set<ContractStatus> getAllowedTransitions() {
            return EnumSet.noneOf(ContractStatus.class); // Terminal state
        }
    },
    
    CANCELLED {
        @Override
        public Set<ContractStatus> getAllowedTransitions() {
            return EnumSet.noneOf(ContractStatus.class); // Terminal state
        }
    };

    /**
     * Returns the set of allowed transitions from this status.
     * @return set of allowed target statuses
     */
    public abstract Set<ContractStatus> getAllowedTransitions();

    /**
     * Validates and performs a state transition.
     * 
     * @param target the target status to transition to
     * @return the target status if transition is allowed
     * @throws TransitionNotAllowedException if the transition is not allowed
     */
    public ContractStatus transitionTo(ContractStatus target) {
        if (!getAllowedTransitions().contains(target)) {
            throw new TransitionNotAllowedException(
                String.format("Cannot transition from %s to %s. Allowed transitions: %s",
                    this, target, getAllowedTransitions())
            );
        }
        return target;
    }

    /**
     * Checks if a transition to the target status is allowed.
     * 
     * @param target the target status
     * @return true if transition is allowed, false otherwise
     */
    public boolean canTransitionTo(ContractStatus target) {
        return getAllowedTransitions().contains(target);
    }
}
