ALTER TABLE pessoa ADD COLUMN cep VARCHAR(9);

ALTER TABLE pessoa ADD COLUMN uf ENUM(
    'AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO',
    'MA', 'MT', 'MS', 'MG', 'PA', 'PB', 'PR', 'PE', 'PI',
    'RJ', 'RN', 'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO'
) DEFAULT NULL;

ALTER TABLE pessoa
ADD COLUMN cidade VARCHAR(100);

ALTER TABLE pessoa
ADD COLUMN bairro VARCHAR(50);

ALTER TABLE pessoa
ADD COLUMN logradouro VARCHAR(100);

ALTER TABLE pessoa
ADD COLUMN complemento_endereco VARCHAR(100);
