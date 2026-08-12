---
name: Bug
about: Relatar um erro, falha de validação ou comportamento inesperado no sistema.
title: '[FIX] - Descrição curta do problema (ex: Erro ao salvar pessoa sem nome)'
labels: 'bug'
assignees: ''
---

## Descrição do Bug
Explicação clara e objetiva do que está acontecendo de errado no sistema.

Ex.:

Pessoa pode ser criada ao tentar salvar com nome e a aplicação leva a uma pagina de erro 500

## Passo a Passo para Reproduzir o Erro
1. Acesse a tela 'X' ou envie a requisição `POST /pessoa'
2. Preencha os campos com '...', exceto o nome
3. Clique em 'Salvar'
4. **Comportamento Inesperado:** A aplicação exibe erro 500

## Comportamento Esperado
O que **deveria** acontecer após a ação (ex: *O sistema deveria exibir a mensagem 'Campo Nome é obrigatório' acima do input*).

## Onde está o problema? (Marque uma ou mais opções)
- [ ] **Frontend (Angular):** Componente, validação de formulário ou chamada de API
- [ ] **Backend (Quarkus):** Regra de negócio no Service, DTO ou Exception Handler
- [ ] **Banco de Dados:** Migration, chave estrangeira ou consulta SQL
- [ ] **Infra / Ambiente:** Configuração do `.env`

## Evidências / Logs (Anexar Prints ou Logs do Console)
> Cole aqui o log de erro do terminal do Quarkus, erro do Inspect do navegador (F12) ou cole um print da tela.

---

## Checklist de Resolução (Definition of Done)
- [ ] Bug corrigido e verificado localmente
- [ ] Criado/Ajustado teste unitário ou de integração para evitar que o erro volte a acontecer