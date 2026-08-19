# Modelo de documento de contrato de API

## Entidade (Ex.: Pessoas

### Criar Entidade (Ex.: Criar Pessoas)

- **Rota: /<entidade no plural kebab-case> ex.: pessoas**
- **Método HTTP: `POST | GET | PUT | DELETE`**
- **Status de retornos possíveis:**  `201 Created`, `400 Bad Request`, `404 Not Found`, `409 Conflict`

### Parâmetros de Entrada

| Campo | Tipo | Obrigatório | Validações / Observações |
| --- | --- | --- | --- |
| `nome` | String | Sim | Não pode ser vazio, máx. 150 caracteres |

### Exemplo de Requisição

```json
{
  "nome": "Maria Silva",
  ...
}
```

### Exemplo de Respostas

#### Status 201 Created

```json
{
  "id": 42,
  "nome": "Maria Silva",
  ...
}
```

#### Status 400 Bad Request

(Use exatamente o estilo desse json para erros)

```json
{
  "type": "[https://api.asmosul.org/errors/dados-invalidos](https://api.asmosul.org/errors/dados-invalidos)",
  "title": "Erro de validação",
  "status": 400,
  "detail": "Um ou mais campos não passaram na validação.",
  "instance": "/pessoas",
  "timestamp": "2026-08-19T10:00:00-03:00",
  "erros": [
    { "campo": "nome", "mensagem": "O nome é obrigatório" },
  ]
}
```

#### Status 409 Conflict

```json
{
  "type": "[https://api.asmosul.org/errors/conflito](https://api.asmosul.org/errors/conflito)",
  "title": "Conflito de dados",
  "status": 409,
  "detail": "Conflito de dados",
  "instance": "/pessoas",
  "timestamp": "2026-08-19T10:00:00-03:00",
  "erros": [
    { "campo": "cpf", "mensagem": "Já existe uma pessoa cadastrada com este CPF." }
  ]
}
```
