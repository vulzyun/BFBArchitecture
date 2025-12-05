-- V5: Sample data after schema changes in V4
-- Insert sample clients, vehicles, and contracts for testing

-- Sample clients with new schema (first_name, last_name, license_number, birth_date, address)
INSERT INTO clients (id, first_name, last_name, license_number, birth_date, address) VALUES
('11111111-1111-1111-1111-111111111111', 'Jean', 'Dupont', 'FR123456789', '1985-03-15', '123 Rue de Paris'),
('22222222-2222-2222-2222-222222222222', 'Marie', 'Dubois', 'FR987654321', '1990-07-22', '456 Avenue Victor Hugo'),
('33333333-3333-3333-3333-333333333333', 'Pierre', 'Martin', 'FR555666777', '1988-11-10', '789 Boulevard Haussmann');

-- Sample vehicles with snake_case columns
INSERT INTO vehicles (id, brand, model, motorization, color, registration_plate, purchase_date, status) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Peugeot', '308', 'Diesel', 'Blue', 'AB-123-CD', '2020-01-15', 'AVAILABLE'),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Renault', 'Clio', 'Gasoline', 'Red', 'XY-789-ZA', '2021-06-20', 'AVAILABLE'),
('cccccccc-cccc-cccc-cccc-cccccccccccc', 'Citroën', 'C3', 'Electric', 'White', 'EF-456-GH', '2022-03-10', 'AVAILABLE');
