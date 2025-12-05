-- V4: Add uniqueness constraints for business rules
-- This migration enforces uniqueness rules required by the business domain

-- Drop and recreate clients table with proper structure
DROP TABLE IF EXISTS clients CASCADE;

CREATE TABLE clients (
    id UUID NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    license_number VARCHAR(50) NOT NULL,
    birth_date DATE NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_client_identity UNIQUE (first_name, last_name, birth_date),
    CONSTRAINT uk_client_license_number UNIQUE (license_number)
);

-- Add uniqueness constraint for vehicle registration plate
ALTER TABLE vehicles ADD CONSTRAINT uk_vehicle_registration_plate UNIQUE (registration_plate);

-- Create indexes for performance
CREATE INDEX idx_client_identity ON clients(first_name, last_name, birth_date);
CREATE INDEX idx_client_license ON clients(license_number);
CREATE INDEX idx_vehicle_registration ON vehicles(registration_plate);
