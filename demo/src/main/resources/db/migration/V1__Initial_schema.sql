-- Initial schema for BFB Management System
-- Creates tables for clients, vehicles, and contracts

-- Clients table
CREATE TABLE clients (
    id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_client_email UNIQUE (email)
);

-- Vehicles table
CREATE TABLE vehicles (
    id UUID NOT NULL,
    brand VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_vehicle_status CHECK (status IN ('AVAILABLE', 'RENTED', 'BROKEN', 'MAINTENANCE'))
);

-- Contracts table
CREATE TABLE contracts (
    id UUID NOT NULL,
    client_id UUID NOT NULL,
    vehicle_id UUID NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_contract_status CHECK (status IN ('PENDING', 'IN_PROGRESS', 'LATE', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT chk_contract_dates CHECK (start_date < end_date)
);

-- Indexes for performance
CREATE INDEX idx_client_email ON clients(email);
CREATE INDEX idx_vehicle_dates ON contracts(vehicle_id, start_date, end_date);
CREATE INDEX idx_contract_status ON contracts(status);
