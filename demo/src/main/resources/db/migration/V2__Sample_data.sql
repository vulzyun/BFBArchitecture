-- Sample data for development and testing
-- This migration can be excluded in production by using Flyway profiles

-- Insert sample clients
INSERT INTO clients (client_id, prenom, nom, adresse, num_permis, date_naissance) VALUES 
    ('550e8400-e29b-41d4-a716-446655440000', 'John', 'Doe', '123 Main St', 'DL123456', '1990-01-15'),
    ('550e8400-e29b-41d4-a716-446655440001', 'Jane', 'Smith', '456 Oak Ave', 'DL789012', '1985-05-22'),
    ('550e8400-e29b-41d4-a716-446655440002', 'Bob', 'Johnson', '789 Pine Rd', 'DL345678', '1992-11-30');

-- Insert sample vehicles
INSERT INTO vehicles (id, brand, model, status) VALUES 
    ('650e8400-e29b-41d4-a716-446655440000', 'Toyota', 'Corolla', 'AVAILABLE'),
    ('650e8400-e29b-41d4-a716-446655440001', 'Honda', 'Civic', 'AVAILABLE'),
    ('650e8400-e29b-41d4-a716-446655440002', 'Ford', 'Focus', 'AVAILABLE'),
    ('650e8400-e29b-41d4-a716-446655440003', 'Tesla', 'Model 3', 'BROKEN');
