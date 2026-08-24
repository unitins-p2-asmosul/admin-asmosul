# Contrato de API — Categorias

## Criar Categoria

- **Rota:** `/categorias`
- **Método HTTP:** `POST`
- **Status de retorno previstos:** `201 Created`, `400 Bad Request`, `404 Not Found`, `409 Conflict`
- **Escopo da Sprint 1:** criação de categoria com os campos `nome` e `descricao`.

## Parâmetros de entrada

| Campo | Tipo | Obrigatório | Validações / Observações |
| --- | --- | --- | --- |
| `nome` | String | Sim | Não pode ser vazio. |
| `descricao` | String | Não | Texto simples. |

> `id` não deve ser enviado no cadastro. O campo `ativo`, apesar de existir no levantamento, não deve constar no formulário de criação.

## Exemplo de requisição

```json
{
  "nome": "Associado",
  "descricao": "Pessoa associada à ASMOSUL."
}
```

## Exemplos de respostas

### Status 201 Created

```json
{
  "id": 1,
  "nome": "Associado",
  "descricao": "Pessoa associada à ASMOSUL."
}
```

### Status 400 Bad Request

```json
{
  "type": "https://api.asmosul.org/errors/dados-invalidos",
  "title": "Erro de validação",
  "status": 400,
  "detail": "Um ou mais campos não passaram na validação.",
  "instance": "/categorias",
  "timestamp": "2026-08-24T13:00:00-03:00",
  "erros": [
    { "campo": "nome", "mensagem": "O nome é obrigatório" }
  ]
}
```

### Status 404 Not Found

```json
{
  "type": "https://api.asmosul.org/errors/recurso-nao-encontrado",
  "title": "Recurso não encontrado",
  "status": 404,
  "detail": "Categoria não encontrada.",
  "instance": "/categorias",
  "timestamp": "2026-08-24T13:00:00-03:00"
}
```

### Status 409 Conflict

```json
{
  "type": "https://api.asmosul.org/errors/conflito",
  "title": "Conflito de dados",
  "status": 409,
  "detail": "Já existe uma categoria cadastrada com este nome.",
  "instance": "/categorias",
  "timestamp": "2026-08-24T13:00:00-03:00"
}
```
