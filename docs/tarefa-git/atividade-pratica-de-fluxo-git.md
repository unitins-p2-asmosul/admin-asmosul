# Atividade Prática de fluxo Git & GitHub

**Todos participarem até o dia 19/08**

#### Cenário Inicial

No repositório oficial do projeto, existe a branch `dev`. Dentro do diretório `/docs/tarefa-git/`, você encontrará:

1. Um diretório `/contribuidores` contendo o arquivo `victor.md` (com Nome Completo).
2. Um arquivo `dev-destaque.md` contendo o texto: `Eu sou o melhor (Victor Alexandre)!`.

#### Passo a Passo da Tarefa

**1. Criar a Issue:**

- Vá na aba **Issues** do repositório e selecione o template de **Feature/Tarefa**.
- Preencha o título no padrão: `[DOC] - Colocar <Seu Nome Primeiro> como participante e como melhor dev`.
- Limpe o template mantendo apenas a **Descrição Curta**:
    
    > *"Criar o arquivo `<seu-primeiro-nome>.md` no diretório de contribuidores e modificar o arquivo `dev-destaque.md` para: 'Eu sou o melhor (<seu nome>)!'."*
    > 
- Na barra lateral direita da Issue:
    - Em **Assignees**: Selecione a si mesmo.
    - Em **Projects**: Selecione o Project .
    - Em **Milestone**: Selecione `Sprint 0 - Planejamento Inicial`.
- Entre no Github projects [https://github.com/orgs/unitins-p2-asmosul/projects/1](https://github.com/orgs/unitins-p2-asmosul/projects/1) e arraste o card criado para “Em progresso”

**2. Criar a Branch:**

- Na página da Issue criada, clique no painel lateral em **Create a branch** (ou crie localmente).
- Escolha o prefixo `docs/` mantendo a sugestão nativa do GitHub (Exemplo: `docs/3-colocar-camila-como-participante`).

**3. Executar as Alterações e Commits (2 Commits Obrigatórios):**

- **Commit 1:** Crie a sua ficha em `/docs/tarefa-git/contribuidores/<seu-primeiro-nome>.md` contendo seu Nome Completo
    - *Mensagem do commit:* `docs: adiciona-arquivo-md-<seunome>`
- **Commit 2:** Edite o arquivo `/docs/tarefa-git/dev-destaque.md` substituindo o nome do colega pelo seu Nome Completo.
    - *Mensagem do commit:* `docs: adiciona-arquivo-destaque-<seunome>`

**4. Abrir o Pull Request (PR):**

- Envie a branch para o GitHub (`git push`) e abra o Pull Request apontando para a branch (ou `dev`).
- Na descrição do Pull Request, cole o seguinte texto
    
    ```markdown
    Foi criado o arquivo de dados pessoais e modificado o arquivo destaque.
    
    Closes #<número_da_issue>
    ```
    
    **O Closes é bem importante**, ele irá mover a issue para o quadro certo do kanban, e serve para no fim do Pull Request fechar a issue
    

**5. Resolução de Conflitos:**

- **Atenção:** Como todos alterarão o arquivo `dev-destaque.md` ao mesmo tempo, **haverá conflito** quando o colega anterior fizer a integração!
- Resolva o conflito no Git/GitHub garantindo que o seu nome permaneça no arquivo. Neste arquivo você é o melhor e você vai atropelar o anterior
- Envie uma mensagem no Discord avisando ao Tech Lead (Victor) que o PR está pronto para revisão.

**6. Aprovação, Integração e Limpeza (Ação do Tech Lead / Autor):**

- O Tech Lead fará a integração do PR utilizando exclusivamente a opção **Squash & Merge**, garantindo que os múltiplos commits intermediários sejam unificados em um único commit limpo na branch principal.
- **Após a confirmação do Squash & Merge:** Exclua a sua branch no GitHub.
