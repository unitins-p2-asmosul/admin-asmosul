ALTER TABLE item ADD CONSTRAINT uq_item_nome UNIQUE (nome);
CREATE INDEX idx_estoque_id_item ON estoque (id_item);

