-- Adicionar flag de redefinição de senha no próximo acesso
ALTER TABLE conta
    ADD COLUMN redefinir_senha BOOLEAN NOT NULL DEFAULT TRUE;
