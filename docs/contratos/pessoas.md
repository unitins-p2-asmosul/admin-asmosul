# Contrato de API — Pessoas

## Criar Pessoa

- **Rota:** `/pessoas`
- **Método HTTP:** `POST`
- **Status de retorno previstos:** `201 Created`, `400 Bad Request`, `404 Not Found`, `409 Conflict`
- **Escopo da Sprint 1:** cadastro parcial de pessoa, **sem dados de endereço**.

## Padrão de contrato para enums de domínio

Os campos tipados como enum adotam formato diferenciado para entrada e saída:

- **Na requisição (POST):** enviar uma String com o código do enum.
- **Na resposta (201 Created):** retornar objeto com `codigo` e `descricao`.

### Valores de domínio utilizados neste contrato

**sexo**

- `FEMININO`
- `MASCULINO`
- `PREFIRO_NAO_INFORMAR`

**escolaridade**

- `FUNDAMENTAL_INCOMPLETO`
- `FUNDAMENTAL_COMPLETO`
- `ENSINO_MEDIO_INCOMPLETO`
- `ENSINO_MEDIO_COMPLETO`
- `SUPERIOR_INCOMPLETO`
- `SUPERIOR_COMPLETO`

**rendaFamiliar**

- `MENOS_DE_MIL`
- `ENTRE_MIL_E_DOIS_MIL`
- `ENTRE_DOIS_MIL_E_TRES_MIL`
- `MAIS_DE_TRES_MIL`

## Parâmetros de entrada

| Campo | Tipo | Obrigatório | Validações / Observações |
| --- | --- | --- | --- |
| `nome` | String | Sim | Não pode ser vazio. |
| `cpf` | String | Sim | Informar apenas números, com 11 dígitos. Deve ser ÚNICO. |
| `dataNascimento` | String/Data | Sim | Padrão brasileiro `dd-mm-aaaa`. |
| `sexo` | String (enum) | Não | Valores aceitos: `FEMININO`, `MASCULINO`, `PREFIRO_NAO_INFORMAR`. |
| `telefone` | String | Sim | Apenas números. Deve possuir 10 ou 11 dígitos. Cada pessoa possui somente um telefone neste escopo. |
| `email` | String | Não | Quando informado, deve possuir `@`. |
| `escolaridade` | String (enum) | Não | Deve utilizar um dos códigos definidos no domínio de escolaridade. |
| `profissao` | String | Não | Texto simples. |
| `rendaFamiliar` | String (enum) | Não | Deve utilizar um dos códigos definidos no domínio de renda familiar. |
| `comorbidades` | Array<Long> | Não | Lista de identificadores de comorbidades relacionadas à pessoa. |
| `categorias` | Array<Long> | Não | Lista de identificadores de categorias relacionadas à pessoa. |
| `descricao` | String | Não | Campo de texto livre para informações não contempladas nos demais atributos. |

> `id` não deve ser enviado no cadastro. Os campos de endereço (`cep`, `uf`, `cidade`, `bairro`, `logradouro` e `complemento`) não fazem parte da Sprint 1.

## Exemplo de requisição

```json
{
  "nome": "Maria Silva",
  "cpf": "12345678901",
  "dataNascimento": "19-08-1995",
  "sexo": "FEMININO",
  "telefone": "63999998888",
  "email": "maria.silva@email.com",
  "escolaridade": "SUPERIOR_COMPLETO",
  "profissao": "Assistente administrativa",
  "rendaFamiliar": "ENTRE_DOIS_MIL_E_TRES_MIL",
  "comorbidades": [1, 2],
  "categorias": [1, 3],
  "descricao": "Pessoa cadastrada para acompanhamento da ASMOSUL."
}
```

## Exemplos de respostas

### Status 201 Created

```json
{
  "id": 42,
  "nome": "Maria Silva",
  "cpf": "12345678901",
  "dataNascimento": "19-08-1995",
  "sexo": {
    "codigo": "FEMININO",
    "descricao": "Feminino"
  },
  "telefone": "63999998888",
  "email": "maria.silva@email.com",
  "escolaridade": {
    "codigo": "SUPERIOR_COMPLETO",
    "descricao": "Superior Completo"
  },
  "profissao": "Assistente administrativa",
  "rendaFamiliar": {
    "codigo": "ENTRE_DOIS_MIL_E_TRES_MIL",
    "descricao": "Entre dois mil e três mil"
  },
  "comorbidades": [1, 2],
  "categorias": [1, 3],
  "descricao": "Pessoa cadastrada para acompanhamento da ASMOSUL."
}
```

### Status 400 Bad Request

```json
{
  "type": "https://api.asmosul.org/errors/dados-invalidos",
  "title": "Erro de validação",
  "status": 400,
  "detail": "Um ou mais campos não passaram na validação.",
  "instance": "/pessoas",
  "timestamp": "2026-08-24T13:00:00-03:00",
  "erros": [
    { "campo": "nome", "mensagem": "O nome é obrigatório" },
    { "campo": "cpf", "mensagem": "O CPF deve conter 11 números" }
  ]
}
```

### Status 404 Not Found

Exemplo para o caso em que uma categoria ou comorbidade informada no cadastro não exista:

```json
{
  "type": "https://api.asmosul.org/errors/recurso-nao-encontrado",
  "title": "Recurso não encontrado",
  "status": 404,
  "detail": "Categoria não encontrada.",
  "instance": "/pessoas",
  "timestamp": "2026-08-24T13:00:00-03:00"
}
```

### Status 409 Conflict

```json
{
  "type": "https://api.asmosul.org/errors/conflito",
  "title": "Conflito de dados",
  "status": 409,
  "detail": "Já existe uma pessoa cadastrada com este CPF",
  "instance": "/pessoas",
  "timestamp": "2026-08-24T13:00:00-03:00"
}
```
