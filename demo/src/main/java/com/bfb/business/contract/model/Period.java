package com.bfb.business.contract.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public record Period(LocalDate startDate, LocalDate endDate) {
    
    public Period {
        Objects.requireNonNull(startDate, "Start date cannot be null");
        Objects.requireNonNull(endDate, "End date cannot be null");
        
        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException(
                String.format("Start date (%s) must be before end date (%s)", startDate, endDate)
            );
        }
    }
    
    public boolean overlapsWith(Period other) {
        Objects.requireNonNull(other, "Cannot check overlap with null period");
        
        return !this.endDate.isBefore(other.startDate) 
            && !other.endDate.isBefore(this.startDate);
    }
    
    public boolean hasEndedBefore(LocalDate date) {
        Objects.requireNonNull(date, "Date cannot be null");
        return endDate.isBefore(date);
    }
    
    public boolean contains(LocalDate date) {
        Objects.requireNonNull(date, "Date cannot be null");
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
    
    public long durationInDays() {
        return ChronoUnit.DAYS.between(startDate, endDate);
    }
    
    public boolean isAfter(LocalDate referenceDate) {
        Objects.requireNonNull(referenceDate, "Reference date cannot be null");
        return startDate.isAfter(referenceDate);
    }
    
    public static Period of(LocalDate startDate, LocalDate endDate) {
        return new Period(startDate, endDate);
    }
    
    @Override
    public String toString() {
        return String.format("Period[%s to %s]", startDate, endDate);
    }
}
