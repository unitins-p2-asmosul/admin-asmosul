---
name: Feature
about: Implementação de novas funcionalidades no projeto  
title: '[BE | FE | BD | DOC | INFRA] - Título da Tarefa (ex.: Criar endpoint para cadastro e atualização de pessoas)'
labels: 'feature'
assignees: ''
---    

**História de usuário correspondente:** US-04
**Dependências:** US-02
**Link do Figma (caso frontend):**

## Descrição curta

Descrição objetiva do que deve ser implementado e qual problema essa tarefa resolve.

ex.:
Cria o endpoint POST e PUT na API para cadastrar e atualizar uma pessoa

## Critérios de aceitação

- [ ] Teste de funcionamento de todos os endpoints criados
- [ ] Teste de funcionamento dos services em caso de lógica complexa
- [ ] Retornar erro amigável em caso de cadastro duplicado de CPF
- [ ] Serão adicionados aqui outros critérios de aceitação conforme a feature

## Endpoints criados (para caso de tarefa no back)

- `POST /pessoas/`
- `PUT /pessoas/{id}`

## Telas e componentes criados (para caso de tarefa no front)

- `pessoa-form.component`
- `pessoa-form.page`

## Tabelas ou índices criados (para caso de tarefa no banco de dados)

- `pessoa`
- `telefone`
- `pessoa_telefone_fk`
