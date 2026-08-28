-- 1. Limpeza das colunas antigo 'ativo' em comorbidade e categoria
ALTER TABLE comorbidade DROP COLUMN ativo;
ALTER TABLE categoria DROP COLUMN ativo;