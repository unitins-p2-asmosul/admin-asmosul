# Contrato de API — Contas

Gestão de contas de acesso de pessoas físicas e de seus perfis (US-43 a US-46, US-49 a US-51, US-53 a US-59).

## Resumo das rotas

| Operação | Rota | Método HTTP | Status de retorno possíveis |
| --- | --- | --- | --- |
| Criar conta | `/contas` | `POST` | `201 Created`, `400 Bad Request`, `404 Not Found`, `409 Conflict` |
| Listar contas | `/contas` | `GET` | `200 OK` |
| Listar perfis | `/contas/perfis` | `GET` | `200 OK` |
| Detalhar conta | `/contas/{id}` | `GET` | `200 OK`, `404 Not Found` |
| Alterar nome de usuário | `/contas/{id}` | `PUT` | `200 OK`, `400 Bad Request`, `404 Not Found`, `409 Conflict` |
| Atualizar perfis | `/contas/{id}/perfis` | `PUT` | `200 OK`, `400 Bad Request`, `404 Not Found` |
| Redefinir senha (administrativo) | `/contas/{id}/redefinir-senha` | `PATCH` | `204 No Content`, `400 Bad Request`, `404 Not Found` |
| Alterar a própria senha | `/contas/minha-senha` | `PATCH` | `204 No Content`, `400 Bad Request`, `401 Unauthorized`, `404 Not Found` |
| Desativar conta | `/contas/{id}/desativar` | `PATCH` | `204 No Content`, `404 Not Found` |
| Reativar conta | `/contas/{id}/reativar` | `PATCH` | `204 No Content`, `400 Bad Request`, `404 Not Found` |

## Padrão de contrato para enums de domínio

- **Na requisição (POST / PUT):** `perfis` é uma **lista de strings** com o código do perfil (ex.: `["GERENCIADOR_ACESSO"]`). O código é aceito sem diferenciar maiúsculas e minúsculas. Não envie o objeto `{ "codigo": ... }`.
- **Na resposta (GET / 201 Created / 200 OK):** cada perfil é retornado como objeto com `codigo` e `descricao`.

### Valores de domínio utilizados neste contrato

**perfis**

| Código | Descrição |
| --- | --- |
| `GERENCIADOR_PESSOAS` | Gerenciador de Pessoas |
| `GERENCIADOR_DOACOES` | Gerenciador de Doações |
| `GERENCIADOR_CAPACITACOES` | Gerenciador de Capacitações |
| `GERENCIADOR_ACESSO` | Gerenciador de Acesso |
| `GERENCIADOR_RELATORIOS` | Gerenciador de Relatórios |

---

## Criar Conta

- **Rota:** `/contas`
- **Método HTTP:** `POST`
- **Status de retorno previstos:** `201 Created`, `400 Bad Request`, `404 Not Found`, `409 Conflict`

### Parâmetros de entrada

| Campo | Tipo | Obrigatório | Validações / Observações |
| --- | --- | --- | --- |
| `pessoaId` | Long | Sim | A pessoa deve existir, estar ativa e ser pessoa física (RN018). A pessoa não pode possuir outra conta (RN010). Pessoas sem e-mail são aceitas. |
| `nomeUsuario` | String | Sim | Não pode ser vazio, máx. 100 caracteres. Deve ser ÚNICO. |
| `senhaTemporaria` | String | Sim | Não pode ser vazia, máx. 72 bytes em UTF-8. Apenas o hash (BCrypt) é gravado. |
| `perfis` | Array<String> | Sim | Ao menos um perfil. Valores aceitos: códigos do domínio **perfis**. |

> A conta é criada com `redefinirSenha = true` e com a data de criação preenchida automaticamente. A senha e o hash nunca são retornados.

### Exemplo de requisição

```json
{
  "pessoaId": 42,
  "nomeUsuario": "maria.silva",
  "senhaTemporaria": "Temporaria@2026",
  "perfis": ["GERENCIADOR_ACESSO", "GERENCIADOR_PESSOAS"]
}
```

### Exemplo de resposta — Status 201 Created

Header `Location: /contas/7`

```json
{
  "id": 7,
  "pessoaId": 42,
  "nomePessoa": "Maria Silva",
  "email": "maria.silva@email.com",
  "nomeUsuario": "maria.silva",
  "perfis": [
    { "codigo": "GERENCIADOR_PESSOAS", "descricao": "Gerenciador de Pessoas" },
    { "codigo": "GERENCIADOR_ACESSO", "descricao": "Gerenciador de Acesso" }
  ],
  "redefinirSenha": true,
  "ativo": true,
  "dataCriacao": "30-09-2026",
  "dataInativo": null
}
```

---

## Listar Contas

- **Rota:** `/contas`
- **Método HTTP:** `GET`
- **Status de retorno previstos:** `200 OK`

### Parâmetros de consulta (query params)

| Parâmetro | Tipo | Obrigatório | Observações |
| --- | --- | --- | --- |
| `nomePessoa` | String | Não | Busca parcial, sem diferenciar maiúsculas e minúsculas. |
| `nomeUsuario` | String | Não | Busca parcial, sem diferenciar maiúsculas e minúsculas. |
| `email` | String | Não | Busca parcial no e-mail da pessoa vinculada. |
| `perfis` | String (repetível) | Não | Ex.: `perfis=GERENCIADOR_ACESSO&perfis=GERENCIADOR_PESSOAS`. Retorna contas com **qualquer um** dos perfis. |
| `redefinirSenha` | Boolean | Não | Filtra pela flag de redefinição de senha. |
| `dataCriacao` | String/Data | Não | Formato `dd-mm-aaaa`. Considera o dia inteiro. |
| `apenasInativos` | Boolean | Não | Padrão `false` (lista apenas contas ativas). Com `true`, lista apenas contas desativadas e os demais filtros operam sobre elas. |
| `dataInativo` | String/Data | Não | Formato `dd-mm-aaaa`. **Só é aplicado com `apenasInativos=true`**. |
| `page`, `size`, `sort` | — | Não | Padrão `size=10`, `sort=nomeUsuario`. Ordenação permitida: `id`, `nomeUsuario`, `dataCriacao`, `dataInativo`, `redefinirSenha`. Campos fora da lista são ignorados. |

### Exemplo de resposta — Status 200 OK

```json
{
  "dados": [
    {
      "id": 7,
      "nomePessoa": "Maria Silva",
      "email": "maria.silva@email.com",
      "nomeUsuario": "maria.silva",
      "perfis": [
        { "codigo": "GERENCIADOR_ACESSO", "descricao": "Gerenciador de Acesso" }
      ],
      "redefinirSenha": true,
      "ativo": true,
      "dataCriacao": "30-09-2026"
    }
  ],
  "paginaAtual": 0,
  "tamanhoPagina": 10,
  "totalElementos": 1,
  "totalPaginas": 1
}
```

---

## Listar Perfis

- **Rota:** `/contas/perfis`
- **Método HTTP:** `GET`
- **Status de retorno previstos:** `200 OK`

Retorna a lista estática de perfis com `codigo` e `descricao`, na ordem da tabela do domínio **perfis**.

---

## Detalhar Conta

- **Rota:** `/contas/{id}`
- **Método HTTP:** `GET`
- **Status de retorno previstos:** `200 OK`, `404 Not Found`

> Retorna contas **ativas e inativas** (US-54). Para contas inativas, `ativo` é `false` e `dataInativo` é preenchida. A resposta segue o mesmo formato do `201 Created` do cadastro.

---

## Alterar Nome de Usuário

- **Rota:** `/contas/{id}`
- **Método HTTP:** `PUT`
- **Status de retorno previstos:** `200 OK`, `400 Bad Request`, `404 Not Found`, `409 Conflict`

| Campo | Tipo | Obrigatório | Validações / Observações |
| --- | --- | --- | --- |
| `nomeUsuario` | String | Sim | Não pode ser vazio, máx. 100 caracteres. Deve ser ÚNICO. |

> Apenas o nome de usuário pode ser alterado; a pessoa vinculada não pode ser trocada (RN019). Um `pessoaId` enviado no corpo é ignorado. Conta inativa retorna `404`.

---

## Atualizar Perfis

- **Rota:** `/contas/{id}/perfis`
- **Método HTTP:** `PUT`
- **Status de retorno previstos:** `200 OK`, `400 Bad Request`, `404 Not Found`

| Campo | Tipo | Obrigatório | Validações / Observações |
| --- | --- | --- | --- |
| `perfis` | Array<String> | Sim | Ao menos um perfil. Substitui o conjunto atual de perfis. |

```json
{ "perfis": ["GERENCIADOR_DOACOES", "GERENCIADOR_RELATORIOS"] }
```

---

## Redefinir Senha (administrativo — US-59)

- **Rota:** `/contas/{id}/redefinir-senha`
- **Método HTTP:** `PATCH`
- **Status de retorno previstos:** `204 No Content`, `400 Bad Request`, `404 Not Found`

| Campo | Tipo | Obrigatório | Validações / Observações |
| --- | --- | --- | --- |
| `novaSenhaTemporaria` | String | Sim | Não pode ser vazia, máx. 72 bytes em UTF-8. |

> Marca `redefinirSenha = true`. Conta inativa retorna `404`.

---

## Alterar a Própria Senha (US-46)

- **Rota:** `/contas/minha-senha`
- **Método HTTP:** `PATCH`
- **Status de retorno previstos:** `204 No Content`, `400 Bad Request`, `401 Unauthorized`, `404 Not Found`
- **Autenticação:** obrigatória (`Authorization: Bearer <token>`). A conta é identificada **somente pelo token**; não se envia id.

| Campo | Tipo | Obrigatório | Validações / Observações |
| --- | --- | --- | --- |
| `senhaAtual` | String | Sim | Deve corresponder à senha atual da conta. |
| `novaSenha` | String | Sim | Não pode ser vazia, máx. 72 bytes em UTF-8. |

> Pode ser usada a qualquer momento, inclusive no primeiro acesso. Marca `redefinirSenha = false`.

---

## Desativar e Reativar Conta

- **Rotas:** `/contas/{id}/desativar` e `/contas/{id}/reativar`
- **Método HTTP:** `PATCH`
- **Status de retorno previstos:** `204 No Content`, `400 Bad Request` (somente reativação), `404 Not Found`

> A desativação exige conta ativa (conta já inativa retorna `404`). A reativação só é permitida se a pessoa vinculada estiver ativa (RN02); reativar uma conta já ativa retorna `204` sem alterações.

---

## Exemplos de erros

> Os exemplos seguem o estilo de `modelo.md`. **Divergência conhecida da implementação atual:** a API retorna `type` no domínio `https://api.asmosul.org.br/erros/...` e, nos erros de regra de negócio (400), um mapa `invalidFields` (`{ "campo": "mensagem" }`) no lugar da lista `erros`. Erros de JSON malformado ou de enum inválido (ex.: perfil inexistente) retornam `400` no formato padrão do Spring (`title: "Bad Request"`, sem lista de campos).

### Status 400 Bad Request

```json
{
  "type": "https://api.asmosul.org/errors/dados-invalidos",
  "title": "Erro de validação",
  "status": 400,
  "detail": "Um ou mais campos não passaram na validação.",
  "instance": "/contas",
  "timestamp": "2026-09-30T10:00:00-03:00",
  "erros": [
    { "campo": "pessoaId", "mensagem": "Apenas pessoas físicas podem possuir contas." }
  ]
}
```

Outras mensagens possíveis:

| Campo | Mensagem |
| --- | --- |
| `pessoaId` | `A pessoa informada está inativa.` |
| `pessoaId` | `A pessoa vinculada está inativa.` (reativação) |
| `senhaTemporaria`, `novaSenhaTemporaria`, `novaSenha` | `A senha deve ter no máximo 72 bytes.` |
| `senhaAtual` | `A senha atual está incorreta.` |

### Status 401 Unauthorized

```json
{
  "type": "https://api.asmosul.org/errors/nao-autorizado",
  "title": "Não Autorizado",
  "status": 401,
  "detail": "Autenticação necessária para acessar este recurso.",
  "instance": "/contas/minha-senha",
  "timestamp": "2026-09-30T10:00:00-03:00"
}
```

### Status 404 Not Found

```json
{
  "type": "https://api.asmosul.org/errors/recurso-nao-encontrado",
  "title": "Recurso não encontrado",
  "status": 404,
  "detail": "Conta ativa não encontrada com o ID informado: 7",
  "instance": "/contas/7",
  "timestamp": "2026-09-30T10:00:00-03:00"
}
```

### Status 409 Conflict

```json
{
  "type": "https://api.asmosul.org/errors/conflito",
  "title": "Conflito de dados",
  "status": 409,
  "detail": "Já existe uma conta com este nome de usuário.",
  "instance": "/contas",
  "timestamp": "2026-09-30T10:00:00-03:00"
}
```

Outra mensagem possível: `Esta pessoa já possui uma conta vinculada.` (RN010).
