-- Flyway Migration V3__Create_audit_and_functions.sql
-- Versão: 3.0
-- Descrição: Criação de tabela de auditoria e funções auxiliares

-- Create audit table for tracking changes
CREATE TABLE inventory_audit (
    id BIGSERIAL PRIMARY KEY,
    inventory_id UUID NOT NULL,
    operation VARCHAR(20) NOT NULL,
    old_values JSONB,
    new_values JSONB,
    changed_by VARCHAR(100),
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_audit_operation CHECK (operation IN ('INSERT', 'UPDATE', 'DELETE'))
);

-- Index for audit queries
CREATE INDEX idx_inventory_audit_inventory_id ON inventory_audit(inventory_id);
CREATE INDEX idx_inventory_audit_changed_at ON inventory_audit(changed_at);
CREATE INDEX idx_inventory_audit_operation ON inventory_audit(operation);

-- Create view for inventory summary
CREATE VIEW inventory_summary AS
SELECT
    s.store_code,
    s.name as store_name,
    s.city,
    s.state,
    i.product_sku,
    i.available_quantity,
    i.reserved_quantity,
    i.committed_quantity,
    (i.available_quantity + i.reserved_quantity + i.committed_quantity) as total_quantity,
    i.last_updated,
    CASE
        WHEN i.available_quantity = 0 THEN 'OUT_OF_STOCK'
        WHEN i.available_quantity <= 5 THEN 'LOW_STOCK'
        WHEN i.available_quantity <= 20 THEN 'MEDIUM_STOCK'
        ELSE 'HIGH_STOCK'
    END as stock_level
FROM inventory i
JOIN stores s ON i.store_id = s.id
WHERE s.status = 'ACTIVE'
ORDER BY s.store_code, i.product_sku;

-- Function to simulate stock updates (for testing)
CREATE OR REPLACE FUNCTION simulate_stock_update(
    p_store_id UUID,
    p_product_sku VARCHAR(12),
    p_quantity_change INTEGER
) RETURNS TABLE(success BOOLEAN, message TEXT) AS $$
DECLARE
    current_available INTEGER;
    old_version BIGINT;
BEGIN
    -- Get current available quantity and version
    SELECT available_quantity, version INTO current_available, old_version
    FROM inventory
    WHERE store_id = p_store_id AND product_sku = p_product_sku;

    IF current_available IS NULL THEN
        RETURN QUERY SELECT FALSE, 'Product not found in inventory';
        RETURN;
    END IF;

    IF current_available + p_quantity_change < 0 THEN
        RETURN QUERY SELECT FALSE, 'Insufficient stock for this operation';
        RETURN;
    END IF;

    -- Update inventory with optimistic locking
    UPDATE inventory
    SET available_quantity = available_quantity + p_quantity_change,
        last_updated = NOW(),
        version = version + 1
    WHERE store_id = p_store_id
      AND product_sku = p_product_sku
      AND version = old_version;

    IF NOT FOUND THEN
        RETURN QUERY SELECT FALSE, 'Concurrent modification detected. Please retry.';
        RETURN;
    END IF;

    RETURN QUERY SELECT TRUE, 'Stock updated successfully';
END;
$$ LANGUAGE plpgsql;

-- Function to clean expired reservations
CREATE OR REPLACE FUNCTION clean_expired_reservations()
RETURNS INTEGER AS $$
DECLARE
    expired_count INTEGER;
BEGIN
    UPDATE stock_reservations
    SET status = 'EXPIRED'
    WHERE status = 'ACTIVE'
      AND expires_at < NOW();

    GET DIAGNOSTICS expired_count = ROW_COUNT;

    RETURN expired_count;
END;
$$ LANGUAGE plpgsql;

-- Comments for documentation
COMMENT ON TABLE inventory_audit IS 'Tabela de auditoria para rastreamento de mudanças no inventário';
COMMENT ON VIEW inventory_summary IS 'Visão consolidada do inventário com informações da loja';
COMMENT ON FUNCTION simulate_stock_update IS 'Função para simular atualizações de estoque com controle de concorrência';
COMMENT ON FUNCTION clean_expired_reservations IS 'Função para limpar reservas expiradas automaticamente';
