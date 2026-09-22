-- Criar tabela de contas
CREATE TABLE conta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_pessoa BIGINT NOT NULL UNIQUE,
    nome_usuario VARCHAR(100) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    data_criacao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_inativo DATETIME NULL,
    CONSTRAINT fk_conta_pessoa FOREIGN KEY (id_pessoa) REFERENCES pessoa(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Criar tabela de perfis de conta
CREATE TABLE conta_perfil (
    id_conta BIGINT NOT NULL,
    perfil VARCHAR(50) NOT NULL,
    PRIMARY KEY (id_conta, perfil),
    CONSTRAINT fk_conta_perfil_conta FOREIGN KEY (id_conta) REFERENCES conta(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
