-- Criar tabela de contas
CREATE TABLE contas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pessoa_id BIGINT NOT NULL UNIQUE,
    nome_usuario VARCHAR(100) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    data_criacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_inativo DATETIME NULL,
    CONSTRAINT fk_contas_pessoas FOREIGN KEY (pessoa_id) REFERENCES pessoas(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Criar tabela de perfis de conta
CREATE TABLE contas_perfis (
    conta_id BIGINT NOT NULL,
    perfil VARCHAR(50) NOT NULL,
    PRIMARY KEY (conta_id, perfil),
    CONSTRAINT fk_contas_perfis_contas FOREIGN KEY (conta_id) REFERENCES contas(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;