CREATE TABLE comorbidade (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE,
    descricao TEXT,
    ativo DATE
);

CREATE TABLE categoria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE,
    descricao TEXT,
    ativo DATE
);

CREATE TABLE pessoa (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    cpf VARCHAR(11) NOT NULL UNIQUE,
    data_nascimentos DATE NOT NULL,
    sexo ENUM('M', 'F', 'OUTRO', 'NAO_INFORMADO'),
    telefone VARCHAR(14),
    email VARCHAR(50) UNIQUE,
    faixa_renda ENUM('MENOS_DE_MIL', 'MIL_DOIS', 'DOIS_TRES', 'MAIS_DE_TRES'),##Menos de mil, entre mil e dois mil, entre dois mil e tres mil, maior que tres mil

    escolaridade ENUM('FUNDAMENTAL_INCOMPLETO', 'FUNDAMENTAL_COMPLETO','MEDIO_INCOMPLETO', 'MEDIO_COMPLETO', 'SUPERIOR_INCOMPLETO', 'SUPERIOR_COMPLETO'),
    profissao VARCHAR(50),
    decricao TEXT,
    ativo DATE
);

CREATE TABLE pessoa_comorbidade (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_pessoa BIGINT NOT NULL,
    id_comorbidade BIGINT NOT NULL,
    CONSTRAINT fk_pessoa_comorbidade_pessoa
        FOREIGN KEY (id_pessoa) REFERENCES pessoa(id) ON DELETE CASCADE,
    CONSTRAINT fk_pessoa_comorbidade_comorbidade
        FOREIGN KEY (id_comorbidade) REFERENCES comorbidade(id) ON DELETE CASCADE
);

CREATE TABLE pessoa_categoria (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_pessoa BIGINT NOT NULL,
    id_categoria BIGINT NOT NULL,
    CONSTRAINT fk_pessoa_categoria_pessoa
        FOREIGN KEY (id_pessoa) REFERENCES pessoa(id) ON DELETE CASCADE,
    CONSTRAINT fk_pessoa_categoria_categoria
        FOREIGN KEY (id_categoria) REFERENCES categoria(id) ON DELETE CASCADE
);
