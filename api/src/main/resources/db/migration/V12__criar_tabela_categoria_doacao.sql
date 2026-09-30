CREATE TABLE categoria_doacao (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE,
    descricao TEXT,
    data_inativo DATETIME DEFAULT NULL
);
