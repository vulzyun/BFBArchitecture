package com.bfb.business.contract.exception;

public class TransitionNotAllowedException extends RuntimeException {
    public TransitionNotAllowedException(String message) {
        super(message);
    }
}
