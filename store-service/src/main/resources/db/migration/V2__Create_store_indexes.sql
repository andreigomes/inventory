-- Store Service Database Indexes
-- V2__Create_store_indexes.sql

SET search_path TO store, public;

-- Indexes for stores table
CREATE INDEX idx_stores_status ON stores(status);
CREATE INDEX idx_stores_manager ON stores(manager_name);
CREATE INDEX idx_stores_location ON stores USING gin(to_tsvector('portuguese', location));

-- Indexes for products table
CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_products_barcode ON products(barcode);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_brand ON products(brand);
CREATE INDEX idx_products_name ON products USING gin(to_tsvector('portuguese', name));
CREATE INDEX idx_products_description ON products USING gin(to_tsvector('portuguese', description));

-- Indexes for categories table
CREATE INDEX idx_categories_name ON categories(name);

-- Indexes for suppliers table
CREATE INDEX idx_suppliers_status ON suppliers(status);
CREATE INDEX idx_suppliers_name ON suppliers(name);

-- Indexes for product_suppliers table
CREATE INDEX idx_product_suppliers_product ON product_suppliers(product_id);
CREATE INDEX idx_product_suppliers_supplier ON product_suppliers(supplier_id);

-- Indexes for alerts table
CREATE INDEX idx_alerts_type ON alerts(type);
CREATE INDEX idx_alerts_severity ON alerts(severity);
CREATE INDEX idx_alerts_status ON alerts(status);
CREATE INDEX idx_alerts_store ON alerts(store_id);
CREATE INDEX idx_alerts_entity ON alerts(entity_type, entity_id);
CREATE INDEX idx_alerts_created_at ON alerts(created_at);

-- Composite indexes for common queries
CREATE INDEX idx_products_category_status ON products(category_id, status);
CREATE INDEX idx_alerts_store_status ON alerts(store_id, status);
CREATE INDEX idx_alerts_type_severity ON alerts(type, severity);
