package com.bfb.business.contract.model;

import com.bfb.business.contract.exception.TransitionNotAllowedException;

import java.util.EnumSet;
import java.util.Set;

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
            return EnumSet.noneOf(ContractStatus.class);
        }
    },
    
    CANCELLED {
        @Override
        public Set<ContractStatus> getAllowedTransitions() {
            return EnumSet.noneOf(ContractStatus.class);
        }
    };

    public abstract Set<ContractStatus> getAllowedTransitions();

    public ContractStatus transitionTo(ContractStatus target) {
        if (!getAllowedTransitions().contains(target)) {
            throw new TransitionNotAllowedException(
                String.format("Cannot transition from %s to %s. Allowed transitions: %s",
                    this, target, getAllowedTransitions())
            );
        }
        return target;
    }

    public boolean canTransitionTo(ContractStatus target) {
        return getAllowedTransitions().contains(target);
    }
}
