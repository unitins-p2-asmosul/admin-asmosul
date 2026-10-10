RENAME TABLE categoria_doacao TO categoria_item;

RENAME TABLE endereco_doacao TO endereco_armazenamento;

CREATE TABLE kit (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 nome VARCHAR(100) NOT NULL,
 descricao TEXT
);

CREATE TABLE item (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(100) NOT NULL,
  id_categoria BIGINT NOT NULL,
  preco_unitario INT,
  unidade_medida ENUM(
    'QUILO',
    'UNIDADE',
    'GRAMA',
    'METRO',
    'CENTIMETRO',
    'MILIMETRO',
    'MILIGRAMA',
    'CAIXA',
    'PACOTE',
    'FARDO',
    'LATA'
  ) NOT NULL,
  data_inativo DATETIME DEFAULT NULL,
  descricao TEXT,
  CONSTRAINT fk_item_categoria FOREIGN KEY (id_categoria) REFERENCES categoria_item(id)
);

CREATE TABLE item_kit (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  id_item BIGINT NOT NULL,
  id_kit BIGINT NOT NULL,
  quantidade INT NOT NULL,
  CONSTRAINT fk_itemkit_item FOREIGN KEY (id_item) REFERENCES item(id),
  CONSTRAINT fk_itemkit_kit FOREIGN KEY (id_kit) REFERENCES kit(id)
);

CREATE TABLE estoque (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 id_item BIGINT NOT NULL,
 id_armazenamento BIGINT NOT NULL,
 quantidade INT NOT NULL DEFAULT 0,
 CONSTRAINT fk_estoque_item FOREIGN KEY (id_item) REFERENCES item(id),
 CONSTRAINT fk_estoque_armazenamento FOREIGN KEY (id_armazenamento) REFERENCES endereco_armazenamento(id),
 CONSTRAINT uq_estoque_item_armazenamento UNIQUE (id_item, id_armazenamento)
);

CREATE TABLE entrada (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 id_doador BIGINT NOT NULL,
 data_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 descricao TEXT,
 CONSTRAINT fk_entrada_pessoa FOREIGN KEY (id_doador) REFERENCES pessoa(id)
);

CREATE TABLE entrada_estoque (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 id_item BIGINT NOT NULL,
 id_armazenamento BIGINT NOT NULL,
 id_entrada BIGINT NOT NULL,
 quantidade INT NOT NULL,
 CONSTRAINT fk_entrada_estoque_item FOREIGN KEY (id_item) REFERENCES item(id),
 CONSTRAINT fk_entrada_estoque_armazenamento FOREIGN KEY (id_armazenamento) REFERENCES endereco_armazenamento(id),
 CONSTRAINT fk_entrada_estoque_entrada FOREIGN KEY (id_entrada) REFERENCES entrada(id)
);

CREATE TABLE saida (
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
   id_recebedor BIGINT NOT NULL,
   data_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
   descricao TEXT,
   CONSTRAINT fk_saida_pessoa FOREIGN KEY (id_recebedor) REFERENCES pessoa(id)
);

CREATE TABLE saida_estoque (
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
   id_item BIGINT NOT NULL,
   id_saida BIGINT NOT NULL,
   id_armazenamento BIGINT NOT NULL,
   quantidade INT NOT NULL,
   CONSTRAINT fk_saida_estoque_item FOREIGN KEY (id_item) REFERENCES item(id),
   CONSTRAINT fk_saida_estoque_saida FOREIGN KEY (id_saida) REFERENCES saida(id),
   CONSTRAINT fk_saida_estoque_armazenamento FOREIGN KEY (id_armazenamento) REFERENCES endereco_armazenamento(id)
);

CREATE TABLE movimentacao (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  id_novo_armazenamento BIGINT NOT NULL,
  data_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  descricao TEXT,
  CONSTRAINT fk_movimentacao_novo_armazenamento FOREIGN KEY (id_novo_armazenamento) REFERENCES endereco_armazenamento(id)
);

CREATE TABLE movimentacao_estoque (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  id_item BIGINT NOT NULL,
  id_armazenamento_antigo BIGINT NOT NULL,
  id_movimentacao BIGINT NOT NULL,
  quantidade INT NOT NULL,
  CONSTRAINT fk_mov_estoque_item FOREIGN KEY (id_item) REFERENCES item(id),
  CONSTRAINT fk_mov_estoque_antigo_armazenamento FOREIGN KEY (id_armazenamento_antigo) REFERENCES endereco_armazenamento(id),
  CONSTRAINT fk_mov_estoque_movimentacao FOREIGN KEY (id_movimentacao) REFERENCES movimentacao(id)
);
