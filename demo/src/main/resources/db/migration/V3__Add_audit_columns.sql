-- Add audit columns to existing tables
-- Adds created_at and updated_at timestamps for tracking entity lifecycle

-- Add audit columns to clients table
ALTER TABLE clients ADD created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE clients ADD updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Add audit columns to vehicles table
ALTER TABLE vehicles ADD created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE vehicles ADD updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Add audit columns to contracts table
ALTER TABLE contracts ADD created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE contracts ADD updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

-- Add indexes for frequently queried audit fields
CREATE INDEX idx_contracts_created_at ON contracts(created_at);
CREATE INDEX idx_clients_created_at ON clients(created_at);
CREATE INDEX idx_vehicles_created_at ON vehicles(created_at);
