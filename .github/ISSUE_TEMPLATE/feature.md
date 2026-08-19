---
name: Feature
about: Implementação de nova funcionalidade ou tarefa técnica
title: '[BE | FE | BD | DOC | INFRA] - Título Objetivo'
labels: 'feature'
assignees: ''
---

## Informações Gerais
* **História de Usuário:** US-XX
* **Dependências:** #ID_da_issue_bloqueante
* **Link Figma:** [Link do protótipo aqui]

---

## Descrição
Descreva brevemente o que precisa ser feito e o objetivo desta entrega.

---

## Critérios de Aceitação (PO)
- [ ] (ex.: Bloquear cadastro com CPF duplicado retornando 409 Conflict)
- [ ] (ex.: O campo 'nome' não pode ser vazio)
- [ ] Testes de API cobrindo cenário de sucesso
- [ ] Testes de API cobrindo cenários de falha (400, 404, 409)
- [ ] Testes de Service cobrindo cenário mais avançado (ex.: regra para estoque de doação abaixo de 0)
- [ ] Documentação do endpoint atualizada no Swagger/OpenAPI

---

### Especificações Técnicas (Preenchido pelo time técnico)

* **Endpoints a serem criados/afetados:** `POST /pessoas`, `PUT /pessoas/{id}`
* **Tabelas/Migrations a serem criadas:** `V1__criar_tabela_pessoas.sql`
* **Componentes a serem criados/afetados:** `FormularioPessoaComponent`
