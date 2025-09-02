-- Insert test data for inventory management system

-- Insert stores
INSERT INTO stores (id, name, location, manager_name, phone, email, status, created_at, updated_at) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'Loja Centro', 'Rua XV de Novembro, 123 - Centro - São Paulo/SP', 'João Silva', '(11) 3333-1111', 'joao.silva@empresa.com', 'ACTIVE', NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440002', 'Loja Shopping Ibirapuera', 'Av. Ibirapuera, 3103 - Ibirapuera - São Paulo/SP', 'Maria Santos', '(11) 3333-2222', 'maria.santos@empresa.com', 'ACTIVE', NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440003', 'Loja Paulista', 'Av. Paulista, 1578 - Bela Vista - São Paulo/SP', 'Carlos Oliveira', '(11) 3333-3333', 'carlos.oliveira@empresa.com', 'ACTIVE', NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440004', 'Loja Vila Madalena', 'Rua Harmonia, 456 - Vila Madalena - São Paulo/SP', 'Ana Costa', '(11) 3333-4444', 'ana.costa@empresa.com', 'MAINTENANCE', NOW(), NOW()),
('550e8400-e29b-41d4-a716-446655440005', 'Loja Morumbi', 'Av. Roque Petroni Jr., 1089 - Morumbi - São Paulo/SP', 'Pedro Lima', '(11) 3333-5555', 'pedro.lima@empresa.com', 'ACTIVE', NOW(), NOW());

-- Insert categories
INSERT INTO categories (id, name, description, created_at, updated_at) VALUES
('660e8400-e29b-41d4-a716-446655440001', 'Eletrônicos', 'Produtos eletrônicos e gadgets', NOW(), NOW()),
('660e8400-e29b-41d4-a716-446655440002', 'Roupas', 'Vestuário masculino e feminino', NOW(), NOW()),
('660e8400-e29b-41d4-a716-446655440003', 'Casa e Jardim', 'Produtos para casa e decoração', NOW(), NOW()),
('660e8400-e29b-41d4-a716-446655440004', 'Esportes', 'Artigos esportivos e fitness', NOW(), NOW()),
('660e8400-e29b-41d4-a716-446655440005', 'Livros', 'Livros e materiais educativos', NOW(), NOW());

-- Insert products
INSERT INTO products (id, name, description, sku, barcode, category_id, unit_price, cost_price, weight, dimensions, brand, model, status, created_at, updated_at) VALUES
('770e8400-e29b-41d4-a716-446655440001', 'Smartphone Galaxy S23', 'Samsung Galaxy S23 128GB Preto', 'SAMS23-128-BLK', '7891234567890', '660e8400-e29b-41d4-a716-446655440001', 2499.99, 1800.00, 0.168, '71.9x146.3x7.6mm', 'Samsung', 'Galaxy S23', 'ACTIVE', NOW(), NOW()),
('770e8400-e29b-41d4-a716-446655440002', 'Notebook Dell Inspiron', 'Dell Inspiron 15 3000 i5 8GB 256GB SSD', 'DELL-INS-15-I5', '7891234567891', '660e8400-e29b-41d4-a716-446655440001', 3299.99, 2400.00, 2.200, '358x247x19.9mm', 'Dell', 'Inspiron 15 3000', 'ACTIVE', NOW(), NOW()),
('770e8400-e29b-41d4-a716-446655440003', 'Camiseta Polo Básica', 'Camiseta Polo 100% Algodão Azul Marinho', 'POL-BAS-AZL-M', '7891234567892', '660e8400-e29b-41d4-a716-446655440002', 89.90, 45.00, 0.200, 'Tamanho M', 'Marca Própria', 'Polo Básica', 'ACTIVE', NOW(), NOW()),
('770e8400-e29b-41d4-a716-446655440004', 'Tênis Nike Air Max', 'Tênis Nike Air Max 270 Preto/Branco', 'NIK-AM270-PB-42', '7891234567893', '660e8400-e29b-41d4-a716-446655440004', 599.99, 350.00, 0.800, 'Tamanho 42', 'Nike', 'Air Max 270', 'ACTIVE', NOW(), NOW()),
('770e8400-e29b-41d4-a716-446655440005', 'Livro Clean Code', 'Clean Code: A Handbook of Agile Software Craftsmanship', 'LIV-CC-ENG', '9780132350884', '660e8400-e29b-41d4-a716-446655440005', 129.90, 80.00, 0.600, '178x235x25mm', 'Prentice Hall', 'Clean Code', 'ACTIVE', NOW(), NOW());

-- Insert inventory for each store
INSERT INTO inventory (id, product_id, store_id, quantity_available, quantity_reserved, quantity_on_order, minimum_stock, maximum_stock, reorder_point, last_restock_date, location_in_store, status, created_at, updated_at) VALUES
-- Loja Centro
('880e8400-e29b-41d4-a716-446655440001', '770e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 25, 3, 10, 5, 50, 10, NOW() - INTERVAL '5 days', 'Seção A - Prateleira 1', 'ACTIVE', NOW(), NOW()),
('880e8400-e29b-41d4-a716-446655440002', '770e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440001', 15, 1, 5, 3, 30, 8, NOW() - INTERVAL '3 days', 'Seção B - Prateleira 2', 'ACTIVE', NOW(), NOW()),
('880e8400-e29b-41d4-a716-446655440003', '770e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440001', 100, 5, 20, 20, 200, 30, NOW() - INTERVAL '7 days', 'Seção C - Prateleira 3', 'ACTIVE', NOW(), NOW()),

-- Loja Shopping Ibirapuera
('880e8400-e29b-41d4-a716-446655440004', '770e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440002', 30, 2, 15, 5, 60, 12, NOW() - INTERVAL '2 days', 'Eletrônicos - A1', 'ACTIVE', NOW(), NOW()),
('880e8400-e29b-41d4-a716-446655440005', '770e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440002', 45, 8, 25, 10, 100, 20, NOW() - INTERVAL '1 day', 'Esportes - B2', 'ACTIVE', NOW(), NOW()),
('880e8400-e29b-41d4-a716-446655440006', '770e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440002', 75, 12, 30, 15, 150, 25, NOW() - INTERVAL '4 days', 'Livros - C3', 'ACTIVE', NOW(), NOW()),

-- Loja Paulista
('880e8400-e29b-41d4-a716-446655440007', '770e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440003', 20, 0, 8, 3, 40, 10, NOW() - INTERVAL '6 days', 'Tech Zone - A', 'ACTIVE', NOW(), NOW()),
('880e8400-e29b-41d4-a716-446655440008', '770e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440003', 80, 15, 40, 20, 180, 35, NOW() - INTERVAL '2 days', 'Fashion - B', 'ACTIVE', NOW(), NOW()),

-- Loja Morumbi
('880e8400-e29b-41d4-a716-446655440009', '770e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440005', 35, 5, 20, 8, 70, 15, NOW() - INTERVAL '1 day', 'Mobile - M1', 'ACTIVE', NOW(), NOW()),
('880e8400-e29b-41d4-a716-446655440010', '770e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440005', 60, 10, 35, 15, 120, 25, NOW() - INTERVAL '3 days', 'Sports - S2', 'ACTIVE', NOW(), NOW()),
('880e8400-e29b-41d4-a716-446655440011', '770e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440005', 90, 18, 45, 20, 200, 40, NOW() - INTERVAL '5 days', 'Books - B3', 'ACTIVE', NOW(), NOW());

-- Insert stock movements (sample transactions)
INSERT INTO stock_movements (id, inventory_id, movement_type, quantity, reference_id, reference_type, reason, performed_by, performed_at, notes) VALUES
('990e8400-e29b-41d4-a716-446655440001', '880e8400-e29b-41d4-a716-446655440001', 'IN', 50, 'PO-2023-001', 'PURCHASE_ORDER', 'Reposição de estoque', 'system', NOW() - INTERVAL '5 days', 'Recebimento do fornecedor Samsung'),
('990e8400-e29b-41d4-a716-446655440002', '880e8400-e29b-41d4-a716-446655440001', 'OUT', 25, 'SALE-2023-001', 'SALE', 'Venda no balcão', 'joao.silva', NOW() - INTERVAL '3 days', 'Venda para cliente João'),
('990e8400-e29b-41d4-a716-446655440003', '880e8400-e29b-41d4-a716-446655440002', 'IN', 20, 'PO-2023-002', 'PURCHASE_ORDER', 'Reposição de estoque', 'system', NOW() - INTERVAL '3 days', 'Recebimento do fornecedor Dell'),
('990e8400-e29b-41d4-a716-446655440004', '880e8400-e29b-41d4-a716-446655440003', 'ADJUSTMENT', -5, 'ADJ-2023-001', 'INVENTORY_ADJUSTMENT', 'Ajuste por avaria', 'maria.santos', NOW() - INTERVAL '2 days', 'Produtos danificados durante transporte'),
('990e8400-e29b-41d4-a716-446655440005', '880e8400-e29b-41d4-a716-446655440004', 'TRANSFER_OUT', 10, 'TRF-2023-001', 'TRANSFER', 'Transferência entre lojas', 'carlos.oliveira', NOW() - INTERVAL '1 day', 'Transferência da Paulista para Ibirapuera');

-- Insert suppliers
INSERT INTO suppliers (id, name, contact_name, email, phone, address, status, created_at, updated_at) VALUES
('aa0e8400-e29b-41d4-a716-446655440001', 'Samsung Brasil', 'Roberto Fernandes', 'contato@samsung.com.br', '(11) 4002-8922', 'Av. Chucri Zaidan, 940 - Vila Cordeiro - São Paulo/SP', 'ACTIVE', NOW(), NOW()),
('aa0e8400-e29b-41d4-a716-446655440002', 'Dell Technologies', 'Ana Carolina', 'vendas@dell.com.br', '(11) 4004-3000', 'Av. Paulista, 37 - Bela Vista - São Paulo/SP', 'ACTIVE', NOW(), NOW()),
('aa0e8400-e29b-41d4-a716-446655440003', 'Nike Brasil', 'Marcos Silva', 'b2b@nike.com.br', '(11) 3021-8000', 'Rua Joaquim Floriano, 466 - Itaim Bibi - São Paulo/SP', 'ACTIVE', NOW(), NOW()),
('aa0e8400-e29b-41d4-a716-446655440004', 'Editora Pearson', 'Julia Santos', 'contato@pearson.com.br', '(11) 3665-1000', 'Rua Nelson Francisco, 26 - Santo Amaro - São Paulo/SP', 'ACTIVE', NOW(), NOW());

-- Insert alerts (low stock and other notifications)
INSERT INTO alerts (id, type, severity, title, message, entity_type, entity_id, store_id, status, created_at, acknowledged_at, resolved_at) VALUES
('bb0e8400-e29b-41d4-a716-446655440001', 'LOW_STOCK', 'HIGH', 'Estoque Baixo - Galaxy S23', 'Produto Galaxy S23 está com estoque abaixo do ponto de reposição na Loja Centro', 'PRODUCT', '770e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440001', 'ACTIVE', NOW() - INTERVAL '2 hours', NULL, NULL),
('bb0e8400-e29b-41d4-a716-446655440002', 'LOW_STOCK', 'MEDIUM', 'Estoque Baixo - Dell Inspiron', 'Produto Dell Inspiron está com estoque abaixo do ponto de reposição na Loja Paulista', 'PRODUCT', '770e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440003', 'ACKNOWLEDGED', NOW() - INTERVAL '1 day', NOW() - INTERVAL '6 hours', NULL),
('bb0e8400-e29b-41d4-a716-446655440003', 'STORE_MAINTENANCE', 'LOW', 'Loja em Manutenção', 'Loja Vila Madalena está em manutenção programada', 'STORE', '550e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440004', 'ACTIVE', NOW() - INTERVAL '12 hours', NULL, NULL);
