# Diretrizes de contribuição do projeto da ASMOSUL

A arquitetura do sistema está definida nos documentos abaixo e deve ser seguida durante o desenvolvimento.

---

## Documentação

### Drive compartilhado

Acesse a seguir para algumas documentações gerais:
https://drive.google.com/drive/folders/1bfP0ZZDRCmkgpsg-K3pjIAHjm1ROC_BD?usp=drive_link

### Arquitetura Backend

```text
docs/padrao-arquitetura/padrao-arquitetura-backend
````

Define os principais padrões do Backend, incluindo organização por módulos, DTOs, Controllers, Services, Repositories, entidades, validações, tratamento de erros, banco de dados, Flyway e testes.

### Arquitetura Frontend

```text
docs/padrao-arquitetura/padrao-arquitetura-frontend
```

Define os padrões do Angular, incluindo Standalone, Signals, organização por features, Pages e Components, Services, Angular Material, Tailwind, rotas e interceptors.

### Contratos da API

```text
docs/contratos/modelo.md
```

Define o padrão dos contratos da API, incluindo rotas, métodos HTTP, parâmetros, validações, enums, respostas e erros.

---

## Como iniciar os projetos

### Backend

Suba a infraestrutura:

```bash
docker compose up -d
```

Depois execute o Backend pela IDE ou Maven.

```bash
./mvnw spring-boot:run
```

O projeto utiliza MySQL e Flyway para gerenciamento das migrations.

### Frontend

Instale as dependências:

```bash
npm install
```

Execute:

```bash
ng serve
```

ou:

```bash
npm start
```

---
## Fluxo de contribuição

As Issues para cada sprint são criadas no seu início. A partir delas, siga o fluxo:

```text
Issue
  ↓
Branch
  ↓
Implementação
  ↓
Commits
  ↓
Push
  ↓
Pull Request
  ↓
Revisão
  ↓
Correções, se necessário
  ↓
Squash & Merge
  ↓
Excluir branch
```

### 1. Criar a Branch

A partir da Issue correspondente, vá no projects, selecione o card, arraste para "fazendo" e crie uma branch utilizando o prefixo adequado.

Exemplo:

```text
docs/3-minha-tarefa
```

### 2. Implementar

Realize somente as alterações relacionadas à Issue.

Siga os padrões definidos em:

```text
docs/padrao-arquitetura/
docs/contratos/
```

### 3. Commits

Faça commits pequenos e relacionados a uma alteração específica.

Utilize o padrão de mensagens definido pelo projeto, por exemplo:

```text
docs: adiciona-arquivo-md-nome
```

### 4. Pull Request

Envie a branch para o GitHub e abra um Pull Request direcionado à branch `dev`.

Na descrição, informe resumidamente o que foi realizado e referencie a Issue correspondente:

```text
Closes #<número-da-issue>
```

O `Closes` deve ser utilizado para que a Issue seja encerrada automaticamente após a integração do PR.

### 5. Revisão e conflitos

Aguarde a revisão do Pull Request.

Caso ocorram conflitos com alterações realizadas por outros contribuidores, resolva os conflitos localmente, mantendo o comportamento correto da sua tarefa, e envie as correções para a mesma branch.

### 6. Integração

Após a aprovação, o Pull Request será integrado utilizando **Squash & Merge**.

Depois da confirmação da integração, exclua a branch utilizada na tarefa.
