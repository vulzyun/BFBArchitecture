-- Sample data for development and testing
-- This migration can be excluded in production by using Flyway profiles

-- Insert sample clients
INSERT INTO clients (id, name, email) VALUES 
    ('550e8400-e29b-41d4-a716-446655440000', 'John Doe', 'john.doe@example.com'),
    ('550e8400-e29b-41d4-a716-446655440001', 'Jane Smith', 'jane.smith@example.com'),
    ('550e8400-e29b-41d4-a716-446655440002', 'Bob Johnson', 'bob.johnson@example.com');

-- Insert sample vehicles
INSERT INTO vehicles (id, brand, model, status) VALUES 
    ('650e8400-e29b-41d4-a716-446655440000', 'Toyota', 'Corolla', 'AVAILABLE'),
    ('650e8400-e29b-41d4-a716-446655440001', 'Honda', 'Civic', 'AVAILABLE'),
    ('650e8400-e29b-41d4-a716-446655440002', 'Ford', 'Focus', 'AVAILABLE'),
    ('650e8400-e29b-41d4-a716-446655440003', 'Tesla', 'Model 3', 'BROKEN');
