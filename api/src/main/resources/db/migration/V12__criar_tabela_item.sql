CREATE TABLE item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    id_categoria BIGINT NOT NULL,
    preco_unitario DOUBLE NOT NULL,
    unidade_medida VARCHAR(20) NOT NULL,
    descricao TEXT,
    data_inativo DATETIME DEFAULT NULL,
    CONSTRAINT fk_item_categoria FOREIGN KEY (id_categoria) REFERENCES categoria (id)
);