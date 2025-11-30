package com.bfb.business.client.model;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing an email address.
 * Immutable and self-validating, encapsulating email validation rules.
 * 
 * Business rules enforced:
 * - Must be non-null and non-blank
 * - Must match valid email format
 * - Maximum length of 100 characters
 * - Case-insensitive comparison (stored as lowercase)
 */
public record Email(String address) {
    
    /**
     * RFC 5322 compliant email pattern (simplified).
     * Validates standard email format: local@domain.tld
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    private static final int MAX_LENGTH = 100;
    
    /**
     * Compact constructor with validation.
     * Normalizes email to lowercase for consistent storage and comparison.
     * 
     * @throws IllegalArgumentException if email is invalid
     */
    public Email {
        Objects.requireNonNull(address, "Email address cannot be null");
        
        String trimmed = address.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Email address cannot be blank");
        }
        
        if (trimmed.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                String.format("Email address must not exceed %d characters. Got: %d", 
                             MAX_LENGTH, trimmed.length())
            );
        }
        
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(
                String.format("Invalid email format: %s", trimmed)
            );
        }
        
        // Normalize to lowercase for consistent storage
        address = trimmed.toLowerCase();
    }
    
    /**
     * Factory method to create an Email from a string.
     * Provides explicit intent in code.
     * 
     * @param emailString the email address string
     * @return a new Email instance
     * @throws IllegalArgumentException if email is invalid
     */
    public static Email of(String emailString) {
        return new Email(emailString);
    }
    
    /**
     * Checks if this email belongs to a specific domain.
     * 
     * @param domain the domain to check (e.g., "example.com")
     * @return true if email is from the specified domain
     */
    public boolean hasDomain(String domain) {
        Objects.requireNonNull(domain, "Domain cannot be null");
        return address.endsWith("@" + domain.toLowerCase());
    }
    
    /**
     * Extracts the local part of the email (before @).
     * 
     * @return the local part of the email address
     */
    public String getLocalPart() {
        int atIndex = address.indexOf('@');
        return address.substring(0, atIndex);
    }
    
    /**
     * Extracts the domain part of the email (after @).
     * 
     * @return the domain of the email address
     */
    public String getDomain() {
        int atIndex = address.indexOf('@');
        return address.substring(atIndex + 1);
    }
    
    @Override
    public String toString() {
        return address;
    }
}
