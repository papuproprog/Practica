-- =============================================================================
-- BASE DATABASES & EXTENSIONS
-- =============================================================================
CREATE DATABASE clientdb;
CREATE DATABASE barberdb;
CREATE DATABASE datedb;
CREATE DATABASE securitydb;

-- SUCURSAL CENTRO
CREATE DATABASE clientdb_centro;
CREATE DATABASE barberdb_centro;
CREATE DATABASE datedb_centro;
CREATE DATABASE securitydb_centro;

-- SUCURSAL NORTE
CREATE DATABASE clientdb_norte;
CREATE DATABASE barberdb_norte;
CREATE DATABASE datedb_norte;
CREATE DATABASE securitydb_norte;

-- SUCURSAL SUR (Opcional adicional)
CREATE DATABASE clientdb_sur;
CREATE DATABASE barberdb_sur;
CREATE DATABASE datedb_sur;
CREATE DATABASE securitydb_sur;

-- =============================================================================
-- 1. POBLAR SUCURSAL CENTRO
-- =============================================================================

-- Security Centro (Usuarios y credenciales)
\c securitydb_centro;

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO users (email, password, role, created_at, updated_at) VALUES
('admin@centro.com', '$2a$10$7klm7.VfnjnklGozpe/3gePmKl.6prOa/cina1FAqQ4xQ3XLLXeEC', 'ADMIN', NOW(), NOW()),
('admin@barber.com', '$2a$10$szK6M/EODoiK1izWBVCLn.ak8miZqi4B4/x/5wUIcmgucficMYe1.', 'ADMIN', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- Barbers Centro (Barberos y Servicios)
\c barberdb_centro;

CREATE TABLE IF NOT EXISTS barbers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS services (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    duration_minutes INTEGER NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO barbers (name, last_name, created_at, updated_at) VALUES
('Carlos', 'Mendoza', NOW(), NOW()),
('Daniel', 'Soto', NOW(), NOW()),
('Luis', 'Hernández', NOW(), NOW());

INSERT INTO services (name, description, duration_minutes, price, created_at, updated_at) VALUES
('Corte de cabello clásico', 'Corte con tijera y máquina con acabado tradicional y lavado', 30, 180.0, NOW(), NOW()),
('Arreglo de barba', 'Recorte, perfilado con navaja e hidratación de aceites naturales', 20, 120.0, NOW(), NOW()),
('Combo Corte y Barba Centro', 'Paquete premium completo con lavado y toalla caliente', 50, 260.0, NOW(), NOW()),
('Afeitado tradicional', 'Afeitado completo a navaja con espuma tibia y loción balsámica', 30, 150.0, NOW(), NOW()),
('Tratamiento facial hidratante', 'Exfoliación profunda, mascarilla y masaje relajante facial', 40, 250.0, NOW(), NOW());

-- Clients Centro
\c clientdb_centro;

CREATE TABLE IF NOT EXISTS clients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO clients (name, last_name, phone, created_at, updated_at) VALUES
('Juan', 'Pérez', '5551234567', NOW(), NOW()),
('Mario', 'Bros', '5559876543', NOW(), NOW()),
('Andrés', 'Castro', '5554567890', NOW(), NOW())
ON CONFLICT (phone) DO NOTHING;

-- Dates Centro
\c datedb_centro;

CREATE TABLE IF NOT EXISTS dates (
    id BIGSERIAL PRIMARY KEY,
    barber BIGINT NOT NULL,
    client BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    service BIGINT NOT NULL,
    service_duration INTEGER DEFAULT 30,
    date_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO dates (barber, client, status, service, service_duration, date_time, created_at, updated_at) VALUES
(1, 1, 'DONE', 3, 50, (CURRENT_DATE + INTERVAL '1 day 10 hours')::TIMESTAMP, NOW(), NOW()),
(2, 2, 'PENDING', 1, 30, (CURRENT_DATE + INTERVAL '2 day 11 hours 30 minutes')::TIMESTAMP, NOW(), NOW()),
(1, 3, 'PENDING', 2, 20, (CURRENT_DATE + INTERVAL '3 day 16 hours')::TIMESTAMP, NOW(), NOW());


-- =============================================================================
-- 2. POBLAR SUCURSAL NORTE
-- =============================================================================

-- Security Norte
\c securitydb_norte;

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO users (email, password, role, created_at, updated_at) VALUES
('admin@norte.com', '$2a$10$q6KFR6EcYBm6KPrVnDXyruZg65PLjZeQPOyhoCTEDkXYBEt7kbxwy', 'ADMIN', NOW(), NOW()),
('admin@barber.com', '$2a$10$szK6M/EODoiK1izWBVCLn.ak8miZqi4B4/x/5wUIcmgucficMYe1.', 'ADMIN', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- Barbers Norte
\c barberdb_norte;

CREATE TABLE IF NOT EXISTS barbers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS services (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    duration_minutes INTEGER NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO barbers (name, last_name, created_at, updated_at) VALUES
('Alejandro', 'Ruiz', NOW(), NOW()),
('Javier', 'López', NOW(), NOW()),
('Rodrigo', 'Morales', NOW(), NOW());

INSERT INTO services (name, description, duration_minutes, price, created_at, updated_at) VALUES
('Fade Urbano Norte', 'Degradado moderno a cero con diseño de navaja en contornos', 35, 200.0, NOW(), NOW()),
('Barba Express', 'Perfilado y alineación rápida con máquina y navaja', 15, 100.0, NOW(), NOW()),
('Combo Norteño VIP', 'Corte personalizado, diseño de barba completa y mascarilla negra', 60, 320.0, NOW(), NOW()),
('Afeitado Black Mask', 'Afeitado tradicional con exfoliación y mascarilla purificante', 45, 220.0, NOW(), NOW()),
('Tinte y Matizado Norte', 'Cubrimiento de canas o decoloración con fijador de color', 70, 400.0, NOW(), NOW());

-- Clients Norte
\c clientdb_norte;

CREATE TABLE IF NOT EXISTS clients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO clients (name, last_name, phone, created_at, updated_at) VALUES
('Roberto', 'Gómez', '5552345678', NOW(), NOW()),
('Esteban', 'Quito', '5558765432', NOW(), NOW()),
('Gabriel', 'Navarro', '5556789012', NOW(), NOW())
ON CONFLICT (phone) DO NOTHING;

-- Dates Norte
\c datedb_norte;

CREATE TABLE IF NOT EXISTS dates (
    id BIGSERIAL PRIMARY KEY,
    barber BIGINT NOT NULL,
    client BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    service BIGINT NOT NULL,
    service_duration INTEGER DEFAULT 30,
    date_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO dates (barber, client, status, service, service_duration, date_time, created_at, updated_at) VALUES
(1, 1, 'PENDING', 1, 35, (CURRENT_DATE + INTERVAL '1 day 12 hours')::TIMESTAMP, NOW(), NOW()),
(2, 2, 'PENDING', 3, 60, (CURRENT_DATE + INTERVAL '2 day 15 hours')::TIMESTAMP, NOW(), NOW()),
(3, 3, 'DONE', 2, 15, (CURRENT_DATE + INTERVAL '1 day 16 hours 30 minutes')::TIMESTAMP, NOW(), NOW());


-- =============================================================================
-- 3. BASE DATABASES (COMPATIBILIDAD TEST SUITE & DEFAULT RUNS)
-- =============================================================================

\c securitydb;

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO users (email, password, role, created_at, updated_at) VALUES
('admin@mishoo.com', '$2a$10$szK6M/EODoiK1izWBVCLn.ak8miZqi4B4/x/5wUIcmgucficMYe1.', 'ADMIN', NOW(), NOW()),
('admin@barber.com', '$2a$10$szK6M/EODoiK1izWBVCLn.ak8miZqi4B4/x/5wUIcmgucficMYe1.', 'ADMIN', NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

\c barberdb;

CREATE TABLE IF NOT EXISTS barbers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS services (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    duration_minutes INTEGER NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO barbers (name, last_name, created_at, updated_at) VALUES
('Carlos', 'Mendoza', NOW(), NOW()),
('Daniel', 'Soto', NOW(), NOW());

INSERT INTO services (name, description, duration_minutes, price, created_at, updated_at) VALUES
('Corte de cabello', 'Corte con tijera y máquina', 30, 180.0, NOW(), NOW()),
('Arreglo de barba', 'Perfilado y recorte', 20, 120.0, NOW(), NOW());

\c clientdb;

CREATE TABLE IF NOT EXISTS clients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO clients (name, last_name, phone, created_at, updated_at) VALUES
('Juan', 'Pérez', '5551234567', NOW(), NOW())
ON CONFLICT (phone) DO NOTHING;

\c datedb;

CREATE TABLE IF NOT EXISTS dates (
    id BIGSERIAL PRIMARY KEY,
    barber BIGINT NOT NULL,
    client BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    service BIGINT NOT NULL,
    service_duration INTEGER DEFAULT 30,
    date_time TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
