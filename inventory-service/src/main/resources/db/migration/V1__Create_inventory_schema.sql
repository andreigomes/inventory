-- Flyway Migration V1__Create_inventory_schema.sql
-- Versão: 1.0
-- Descrição: Criação inicial do schema de inventário com tabelas principais

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create inventory table
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

-- Comments for documentation
COMMENT ON TABLE inventory IS 'Tabela principal de inventário com controle de estoque por loja e produto';
COMMENT ON TABLE stores IS 'Cadastro de lojas físicas do sistema';
COMMENT ON TABLE stock_reservations IS 'Reservas temporárias de estoque com controle de expiração';
