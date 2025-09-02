-- Flyway Migration V2__Create_indexes.sql
-- Versão: 2.0
-- Descrição: Criação de índices para otimização de performance

-- Indexes for inventory table
CREATE INDEX idx_inventory_store_id ON inventory(store_id);
CREATE INDEX idx_inventory_product_sku ON inventory(product_sku);
CREATE INDEX idx_inventory_last_updated ON inventory(last_updated);
CREATE INDEX idx_inventory_low_stock ON inventory(store_id, available_quantity) WHERE available_quantity < 10;
CREATE INDEX idx_inventory_version ON inventory(version); -- Para controle de concorrência otimista

-- Indexes for stores table
CREATE INDEX idx_stores_store_code ON stores(store_code);
CREATE INDEX idx_stores_status ON stores(status);
CREATE INDEX idx_stores_city_state ON stores(city, state); -- Para busca por localização

-- Indexes for stock_reservations table
CREATE INDEX idx_reservations_store_product ON stock_reservations(store_id, product_sku);
CREATE INDEX idx_reservations_expires_at ON stock_reservations(expires_at) WHERE status = 'ACTIVE';
CREATE INDEX idx_reservations_status ON stock_reservations(status);
CREATE INDEX idx_reservations_created_at ON stock_reservations(created_at); -- Para relatórios históricos

-- Composite indexes for common queries
CREATE INDEX idx_inventory_store_available ON inventory(store_id, available_quantity DESC);
CREATE INDEX idx_reservations_active ON stock_reservations(store_id, product_sku, expires_at) WHERE status = 'ACTIVE';
