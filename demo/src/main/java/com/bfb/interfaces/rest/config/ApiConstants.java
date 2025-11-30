package com.bfb.interfaces.rest.config;

/**
 * Centralized constants for API configuration and error handling.
 */
public final class ApiConstants {
    
    private ApiConstants() {
        // Utility class - prevent instantiation
    }

    /**
     * Base URI for API error documentation.
     */
    public static final String ERROR_BASE_URI = "https://bfbmanagement.com/errors";

    /**
     * Standard error type URIs for RFC 7807 Problem Details.
     */
    public static final class ErrorTypes {
        private ErrorTypes() {}
        
        public static final String NOT_FOUND = ERROR_BASE_URI + "/not-found";
        public static final String VALIDATION = ERROR_BASE_URI + "/validation";
        public static final String CONFLICT = ERROR_BASE_URI + "/conflict";
        public static final String TRANSITION_NOT_ALLOWED = ERROR_BASE_URI + "/transition-not-allowed";
        public static final String INTERNAL_ERROR = ERROR_BASE_URI + "/internal";
    }

    /**
     * Standard error titles.
     */
    public static final class ErrorTitles {
        private ErrorTitles() {}
        
        public static final String NOT_FOUND = "Resource not found";
        public static final String VALIDATION_FAILED = "Validation failed";
        public static final String PARAMETER_VALIDATION_FAILED = "Parameter validation failed";
        public static final String BUSINESS_CONFLICT = "Business conflict";
        public static final String TRANSITION_NOT_ALLOWED = "State transition not allowed";
        public static final String INTERNAL_ERROR = "Internal error";
    }

    /**
     * Standard error messages.
     */
    public static final class ErrorMessages {
        private ErrorMessages() {}
        
        public static final String INTERNAL_ERROR = "An internal error occurred";
    }
}
