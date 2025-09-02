-- Database initialization script for PostgreSQL
-- Creates tables with proper indexing for enterprise performance

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create inventory table with partitioning support
CREATE TABLE inventory (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    store_id UUID NOT NULL,
    product_sku VARCHAR(12) NOT NULL,
    available_quantity INTEGER NOT NULL DEFAULT 0,
    reserved_quantity INTEGER NOT NULL DEFAULT 0,
    committed_quantity INTEGER NOT NULL DEFAULT 0,
    last_updated TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_inventory_store_product UNIQUE (store_id, product_sku),
    CONSTRAINT chk_available_quantity CHECK (available_quantity >= 0),
    CONSTRAINT chk_reserved_quantity CHECK (reserved_quantity >= 0),
    CONSTRAINT chk_committed_quantity CHECK (committed_quantity >= 0)
);

-- Indexes for optimal query performance
CREATE INDEX idx_inventory_store_id ON inventory(store_id);
CREATE INDEX idx_inventory_product_sku ON inventory(product_sku);
CREATE INDEX idx_inventory_last_updated ON inventory(last_updated);
CREATE INDEX idx_inventory_low_stock ON inventory(store_id, available_quantity) WHERE available_quantity < 10;

-- Create stock_reservations table
CREATE TABLE stock_reservations (
    reservation_id UUID PRIMARY KEY,
    store_id UUID NOT NULL,
    product_sku VARCHAR(12) NOT NULL,
    quantity INTEGER NOT NULL,
    reason VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT chk_reservation_quantity CHECK (quantity > 0),
    CONSTRAINT chk_reservation_status CHECK (status IN ('ACTIVE', 'COMMITTED', 'RELEASED', 'EXPIRED'))
);

-- Indexes for reservations
CREATE INDEX idx_reservations_store_product ON stock_reservations(store_id, product_sku);
CREATE INDEX idx_reservations_expires_at ON stock_reservations(expires_at) WHERE status = 'ACTIVE';
CREATE INDEX idx_reservations_status ON stock_reservations(status);

-- Create stores table
CREATE TABLE stores (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    store_code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    street VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(50),
    zip_code VARCHAR(20),
    country VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    last_updated TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 1,

    CONSTRAINT chk_store_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'MAINTENANCE'))
);

-- Indexes for stores
CREATE INDEX idx_stores_store_code ON stores(store_code);
CREATE INDEX idx_stores_status ON stores(status);

-- Create audit table for tracking changes
CREATE TABLE inventory_audit (
    id BIGSERIAL PRIMARY KEY,
    inventory_id UUID NOT NULL,
    operation VARCHAR(20) NOT NULL,
    old_values JSONB,
    new_values JSONB,
    changed_by VARCHAR(100),
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Index for audit queries
CREATE INDEX idx_inventory_audit_inventory_id ON inventory_audit(inventory_id);
CREATE INDEX idx_inventory_audit_changed_at ON inventory_audit(changed_at);

-- Insert sample data for testing
INSERT INTO stores (id, store_code, name, street, city, state, zip_code, country) VALUES
    ('11111111-1111-1111-1111-111111111111', 'ST001', 'Loja Centro', 'Rua das Flores, 123', 'São Paulo', 'SP', '01000-000', 'Brasil'),
    ('22222222-2222-2222-2222-222222222222', 'ST002', 'Loja Shopping', 'Av. Paulista, 456', 'São Paulo', 'SP', '01310-100', 'Brasil'),
    ('33333333-3333-3333-3333-333333333333', 'ST003', 'Loja Norte', 'Rua do Norte, 789', 'Rio de Janeiro', 'RJ', '20040-020', 'Brasil');

INSERT INTO inventory (store_id, product_sku, available_quantity, reserved_quantity, committed_quantity) VALUES
    ('11111111-1111-1111-1111-111111111111', 'PROD123456', 100, 0, 0),
    ('11111111-1111-1111-1111-111111111111', 'PROD789012', 50, 5, 10),
    ('22222222-2222-2222-2222-222222222222', 'PROD123456', 75, 0, 0),
    ('22222222-2222-2222-2222-222222222222', 'PROD789012', 30, 2, 5),
    ('33333333-3333-3333-3333-333333333333', 'PROD123456', 25, 0, 0),
    ('33333333-3333-3333-3333-333333333333', 'PROD789012', 40, 3, 2);
