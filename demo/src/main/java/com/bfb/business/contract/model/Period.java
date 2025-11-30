package com.bfb.business.contract.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Value object representing a time period with start and end dates.
 * Immutable and self-validating, encapsulating business rules for date ranges.
 * 
 * Business rules enforced:
 * - Start date must be before end date
 * - Neither date can be null
 * - Provides overlap detection for scheduling conflicts
 */
public record Period(LocalDate startDate, LocalDate endDate) {
    
    /**
     * Compact constructor with validation.
     * Ensures business invariants are maintained at construction time.
     * 
     * @throws IllegalArgumentException if dates are invalid
     */
    public Period {
        Objects.requireNonNull(startDate, "Start date cannot be null");
        Objects.requireNonNull(endDate, "End date cannot be null");
        
        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException(
                String.format("Start date (%s) must be before end date (%s)", startDate, endDate)
            );
        }
    }
    
    /**
     * Checks if this period overlaps with another period.
     * Two periods overlap if they share any common days.
     * 
     * Algorithm: Periods DON'T overlap if:
     * - This period ends before the other starts, OR
     * - The other period ends before this starts
     * Therefore, they DO overlap if neither condition is true.
     * 
     * @param other the other period to check against
     * @return true if periods overlap, false otherwise
     */
    public boolean overlapsWith(Period other) {
        Objects.requireNonNull(other, "Cannot check overlap with null period");
        
        // Periods overlap if NOT (this ends before other starts OR other ends before this starts)
        return !this.endDate.isBefore(other.startDate) 
            && !other.endDate.isBefore(this.startDate);
    }
    
    /**
     * Checks if this period has ended before the given date.
     * Useful for determining if a rental contract is overdue.
     * 
     * @param date the date to check against
     * @return true if the period ended before the given date
     */
    public boolean hasEndedBefore(LocalDate date) {
        Objects.requireNonNull(date, "Date cannot be null");
        return endDate.isBefore(date);
    }
    
    /**
     * Checks if the given date falls within this period (inclusive).
     * 
     * @param date the date to check
     * @return true if date is within the period
     */
    public boolean contains(LocalDate date) {
        Objects.requireNonNull(date, "Date cannot be null");
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
    
    /**
     * Calculates the duration of this period in days.
     * 
     * @return the number of days between start and end (exclusive of end date)
     */
    public long durationInDays() {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }
    
    /**
     * Checks if this period is in the future relative to the given date.
     * 
     * @param referenceDate the date to compare against
     * @return true if this period starts after the reference date
     */
    public boolean isAfter(LocalDate referenceDate) {
        Objects.requireNonNull(referenceDate, "Reference date cannot be null");
        return startDate.isAfter(referenceDate);
    }
    
    /**
     * Creates a Period from two dates, performing validation.
     * Factory method providing explicit intent.
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @return a new Period instance
     */
    public static Period of(LocalDate startDate, LocalDate endDate) {
        return new Period(startDate, endDate);
    }
    
    @Override
    public String toString() {
        return String.format("Period[%s to %s]", startDate, endDate);
    }
}
