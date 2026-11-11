ALTER TABLE kit ADD COLUMN data_inativo DATETIME DEFAULT NULL;
ALTER TABLE kit ADD CONSTRAINT uq_kit_nome UNIQUE (nome);
ALTER TABLE item_kit ADD CONSTRAINT uq_itemkit_item_kit UNIQUE (id_item, id_kit);
CREATE INDEX idx_item_kit_id_kit ON item_kit (id_kit);
