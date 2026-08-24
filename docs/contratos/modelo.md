# Modelo de documento de contrato de API

## Entidade (Ex.: Pessoas

### Criar Entidade (Ex.: Criar Pessoas)

- **Rota: /<entidade no plural kebab-case> ex.: pessoas**
- **Método HTTP: `POST | GET | PUT | DELETE`**
- **Status de retornos possíveis:** `201 Created`, `400 Bad Request`, `404 Not Found`, `409 Conflict`

### Padrão de Contrato para Enums de Domínio

Os campos tipados como Enum adotam formato diferenciado para entrada e saída:

- **Na Requisição (POST / PUT):** Enviar apenas a String com o código do enum (ex.: "MASCULINO").
- **Na Resposta (GET / 201 Created):** A API retorna o objeto estruturado com codigo e descricao.

### Valores de domínio utilizados neste contrato

**Enum 1 (ex.: Sexo)**

- `VALOR_ENUM_1` (ex.: `MASCULINO`)
- `VALOR_ENUM_2` (ex.: `FEMININO`)

---

### Parâmetros de Entrada

| Campo | Tipo | Obrigatório | Validações / Observações |
| --- | --- | --- | --- |
| `nome` | String | Sim | Não pode ser vazio, máx. 150 caracteres |
| `sexo` | String | Sim | valores aceitos: FEMININO, MASCULINO, PREFIRO_NAO_INFORMAR |

> Inserir alguma observação aqui a respeito da Sprint ou dos campos

---

### Exemplo de Requisição

```json
{
  "nome": "Maria Silva",
  "sexo": "FEMININO",
  ...
}
```

### Exemplo de Respostas

#### Status 201 Created

```json
{
  "id": 42,
  "nome": "Maria Silva",
  "sexo": {
    "codigo": "FEMININO",
    "descricao": "Feminino"
  },
  ...
}
```

#### Status 400 Bad Request

(Use exatamente o estilo desse json para erros)

```json
{
  "type": "https://api.asmosul.org/errors/dados-invalidos",
  "title": "Erro de validação",
  "status": 400,
  "detail": "Um ou mais campos não passaram na validação.",
  "instance": "/pessoas",
  "timestamp": "2026-08-19T10:00:00-03:00",
  "erros": [
    { "campo": "nome", "mensagem": "O nome é obrigatório" }
  ]
}
```

#### Status 404 Not Found

```json
{
  "type": "https://api.asmosul.org/errors/recurso-nao-encontrado",
  "title": "Recurso não encontrado",
  "status": 404,
  "detail": "Categoria não encontrada.",
  "instance": "/pessoas",
  "timestamp": "2026-08-19T10:00:00-03:00"
}
```

#### Status 409 Conflict

```json
{
  "type": "https://api.asmosul.org/errors/conflito",
  "title": "Conflito de dados",
  "status": 409,
  "detail": "Já existe uma pessoa cadastrada com este CPF.",
  "instance": "/pessoas",
  "timestamp": "2026-08-19T10:00:00-03:00"
}
```
