# Padrão de Arquitetura do Frontend

**Versão:** 1.1

## Importante!

Não há arquivos estáticos de contrato de API na pasta `docs/`. Toda a integração, tipagem de DTOs, rotas e payloads deve ser consultada diretamente na interface do Swagger em homologação: [https://h.asmosul.site/api/swagger-ui.html](https://h.asmosul.site/api/swagger-ui.html).

## Visão Geral

Este projeto utiliza **Angular (v17+)**, priorizando práticas modernas da plataforma.

### Diretrizes de Implementação

- **Arquitetura 100% Standalone:** o uso de `NgModule` é estritamente proibido. Todos os componentes, diretivas e pipes devem declarar `standalone: true` e importar apenas o que utilizam em seu array `imports: []`.

- **Sintaxe de Fluxo de Controle:**
    - Proibido o uso de diretivas estruturais legadas (`*ngIf`, `*ngFor`, `*ngSwitch`).
    - Utilize exclusivamente a sintaxe de blocos nativa: `@if`, `@else`, `@switch`, `@case` e `@for`.
    - Todo `@for` deve obrigatoriamente declarar a propriedade `track` com um identificador único (ex.: `@for (item of dados(); track item.id)`), acompanhado do bloco `@empty` para coleções vazias.

- **Reatividade com Signals:**
    - Gerencie estados locais e valores mutáveis usando `signal()`.
    - Utilize `computed()` para estados derivados/calculados.
    - Utilize as funções `input()`, `input.required()` e `output()` para comunicação entre componentes.

- **Injeção de Dependências Funcional:** não utilize injeção via construtor. Declare as dependências diretamente no corpo da classe via `inject(...)`.

- **Separação Obrigatória de Arquivos:** formulários, tabelas, páginas e diálogos de filtros devem manter seus arquivos HTML (`.html`) e TypeScript (`.ts`) estritamente separados (proibido o uso de templates *inline*). Mantenha os seletores padronizados com o prefixo `app-` em *kebab-case*.

---

## Configuração Global da Aplicação

A inicialização da aplicação adota uma arquitetura puramente funcional através de providers do Angular, configurando o roteamento com mapeamento automático de parâmetros e dados de rota para inputs dos componentes, além da pipeline HTTP com interceptores globais e captura de erros de runtime.

### Diretrizes de Configuração

- **Roteamento com Binding de Parâmetros:** utiliza obrigatoriamente `withComponentInputBinding()` junto ao `provideRouter`, mapeando parâmetros de URL (`:id`) e metadados (`data`) diretamente como `input()` nos componentes das páginas.

- **Tratamento Global de Erros de Navegador:** registra o provider `provideBrowserGlobalErrorListeners()` para captura e observabilidade de falhas globais não tratadas.

- **Pipeline HTTP com Interceptores Funcionais:** utiliza `provideHttpClient(withInterceptors([...]))` registrando:
    - `apiInterceptorFn`: prefixação dinâmica da URL base da API REST.
    - `erroInterceptorFn`: captura centralizada de falhas HTTP (Problem Details / RFC 7807) e disparo de notificações ao usuário.

### Arquivo de Referência

| Tipo | Arquivo |
| --- | --- |
| Provedores globais da aplicação | `src/app/app.config.ts` |

---

## Estrutura de Diretórios

A arquitetura do frontend organiza o código dividindo responsabilidades entre inicialização global (`app`), serviços/layouts transversais de infraestrutura (`core`) e módulos de negócio (`features`), além de uma área compartilhada interna (`features/shared`).

```
src/
├── app/
│   ├── app.config.ts
│   ├── app.html
│   ├── app.routes.ts
│   ├── app.ts
│   ├── home.component.ts
│   ├── core/
│   │   ├── interceptors/
│   │   │   ├── api.interceptor.ts
│   │   │   └── erro.interceptor.ts
│   │   └── layout/
│   │       ├── footer.component.html
│   │       ├── footer.component.ts
│   │       ├── header.component.html
│   │       ├── header.component.ts
│   │       ├── sidebar.component.html
│   │       └── sidebar.component.ts
│   └── features/
│       ├── pessoas/
│       │   ├── components/
│       │   │   ├── categoria-filtro-dialog/
│       │   │   │   ├── categoria-filtro-dialog.component.html
│       │   │   │   └── categoria-filtro-dialog.component.ts
│       │   │   ├── categoria-tabela/
│       │   │   │   ├── categoria-tabela.component.html
│       │   │   │   └── categoria-tabela.component.ts
│       │   │   ├── comorbidade-filtro-dialog/
│       │   │   │   ├── comorbidade-filtro-dialog.component.html
│       │   │   │   └── comorbidade-filtro-dialog.component.ts
│       │   │   ├── comorbidade-tabela/
│       │   │   │   ├── comorbidade-tabela.component.html
│       │   │   │   └── comorbidade-tabela.component.ts
│       │   │   ├── pessoa-filtro-dialog/
│       │   │   │   ├── pessoa-filtro-dialog.component.html
│       │   │   │   └── pessoa-filtro-dialog.component.ts
│       │   │   └── pessoa-tabela/
│       │   │       ├── pessoa-tabela.component.html
│       │   │       └── pessoa-tabela.component.ts
│       │   ├── mocks/
│       │   │   ├── categoria.mock.ts
│       │   │   ├── comorbidade.mock.ts
│       │   │   └── pessoa.mock.ts
│       │   ├── models/
│       │   │   ├── categoria.model.ts
│       │   │   ├── comorbidade.model.ts
│       │   │   └── pessoa.model.ts
│       │   ├── pages/
│       │   │   ├── categoria-cadastro-page/
│       │   │   │   ├── categoria-cadastro-page.component.html
│       │   │   │   └── categoria-cadastro-page.component.ts
│       │   │   ├── categoria-lista-page/
│       │   │   │   ├── categoria-lista-page.component.html
│       │   │   │   └── categoria-lista-page.component.ts
│       │   │   ├── comorbidade-cadastro-page/
│       │   │   │   ├── comorbidade-cadastro-page.component.html
│       │   │   │   └── comorbidade-cadastro-page.component.ts
│       │   │   ├── comorbidade-lista-page/
│       │   │   │   ├── comorbidade-lista-page.component.html
│       │   │   │   └── comorbidade-lista-page.component.ts
│       │   │   ├── pessoa-cadastro-page/
│       │   │   │   ├── pessoa-cadastro-page.component.html
│       │   │   │   └── pessoa-cadastro-page.component.ts
│       │   │   └── pessoa-lista-page/
│       │   │       ├── pessoa-lista-page.component.html
│       │   │       └── pessoa-lista-page.component.ts
│       │   ├── services/
│       │   │   ├── categoria.service.ts
│       │   │   ├── cep.service.ts
│       │   │   ├── comorbidade.service.ts
│       │   │   └── pessoa.service.ts
│       │   └── pessoas.routes.ts
│       └── shared/
│           ├── components/
│           │   ├── dialogo-confirmacao.component.html
│           │   ├── dialogo-confirmacao.component.ts
│           │   └── pagina-gerenciamento/
│           │       ├── pagina-gerenciamento.component.html
│           │       └── pagina-gerenciamento.component.ts
│           ├── models/
│           │   ├── erro-api.model.ts
│           │   ├── item-dominio.model.ts
│           │   ├── parametros-consulta.model.ts
│           │   └── resposta-paginada.model.ts
│           └── services/
│               ├── dialogo-confirmacao.service.ts
│               └── notificacao.service.ts
├── enviroments/
│   ├── enviroment.prod.ts
│   └── enviroment.ts
├── index.html
├── main.ts
├── material-theme.scss
├── proxy.conf.json
└── styles.css
```

---

## Estilização

O projeto adota uma arquitetura de estilização híbrida, combinando utilitários do Tailwind CSS para estruturação de layout com regras centralizadas no arquivo global (`styles.css`), garantindo consistência com o Design System da Asmosul sem exigir estilos locais nos componentes.

### Diretrizes de Implementação

- **Zero arquivos locais:** é estritamente proibido criar arquivos de estilo `.css`/`.scss` para componentes individuais ou blocos `styles: [...]` inline no TypeScript. Todos os ajustes estruturais residem exclusivamente no `styles.css`.

- **Uso Primário do Tailwind CSS:** utilize as classes utilitárias do Tailwind CSS v4 para diagramação responsiva, grids, flexbox, espaçamentos (margens/paddings) e alinhamentos gerais.

- **Classes Globais Utilitárias:** ao construir novas interfaces, utilize as classes padronizadas do sistema:
    - `.tabela-asmosul`: aplica o padrão de tipografia, espaçamento, alinhamento centralizado e tags de status nas tabelas da aplicação.
    - `.pagina-formulario`: aplica as larguras máximas (`max-w-72rem`), margens verticais externas, overrides das variáveis do Material (`--mat-form-field-*`) para cantos arredondados, fontes padronizadas e mapeamento de botões de ação (`.acao-primaria`, `.acao-perigo`, `.acao-sucesso`).
    - `.pagina-formulario__visualizacao`: trata as cores de borda, opacidade e background para inputs no modo "apenas leitura/desabilitado".

- **Tokens e Temas Globais:** o topo do arquivo define as variáveis de núcleo dentro da diretiva `@theme` do Tailwind (ex.: `--color-asmosul-navy`, `--color-asmosul-sucesso`), as quais são aplicadas diretamente no container da aplicação para fundos e tipografia base (`body { background: #f5f7fa; }`).

### Localização das Configurações Globais

| Finalidade | Arquivo / Diretório |
| --- | --- |
| Importação do Tailwind v4, definição do Design System, overrides do Angular Material e classes utilitárias gerais | `src/styles.css` |

---

## Roteamento e Navegação

A aplicação adota carregamento sob demanda (*lazy loading*) organizado por módulos funcionais, padronização semântica para rotas de CRUD e gestão reativa do menu lateral.

### Diretrizes de Roteamento

- **Carregamento por Feature (*Lazy Loading*):** o roteador raiz (`app.routes.ts`) delega o carregamento das páginas para as rotas específicas de cada feature utilizando `loadChildren()` com *dynamic imports*.

- **Precedência Obrigatória de Sub-recursos:** em arquivos de rotas de feature (ex.: `pessoas.routes.ts`), sub-recursos (como `comorbidades` e `categorias`) devem ser declarados **antes** das rotas da entidade principal que contenham `:id`. Caso contrário, o segmento `:id` capturará o caminho do sub-recurso como identificador numérico.

- **Padrão Semântico de Rotas (CRUD):**
    - **Listagem:** `''` (raiz do recurso ou sub-recurso).
    - **Criação:** `'adicionar'`.
    - **Edição:** `':id/editar'` (reutiliza o componente de formulário com `visualizacao: false`).
    - **Visualização:** `':id'` (reutiliza o componente de formulário com `data: { visualizacao: true }`).

- **Navegação Lateral (`SidebarComponent`):**
    - Gerenciamento reativo através de *Signals* e *Computed*, calculando a rota ativa pelo prefixo mais longo para evitar que rotas pai fiquem simultaneamente selecionadas.
    - Expansão automática da seção correspondente quando o usuário navega em um sub-recurso.
    - Suporte a itens informativos desabilitados (`emDefinicao: true` ou itens sem propriedade `rota`) para recursos ainda não implementados.

### Arquivos de Referência

| Tipo | Arquivo |
| --- | --- |
| Roteamento raiz da aplicação | `src/app/app.routes.ts` |
| Rotas modulares de feature com sub-recursos | `src/app/features/pessoas/pessoas.routes.ts` |
| Componente e template do menu lateral | `src/app/core/layout/sidebar.component.ts` e `sidebar.component.html` |

---

## Camadas da Aplicação: Models / Interfaces

As interfaces TypeScript modelam com precisão os contratos de dados e esquemas do Swagger, garantindo tipagem estrita para requisições, respostas da API, paginação e filtros de consulta.

### Diretrizes de Implementação

- **Fidelidade aos Schemas da API:** as propriedades das interfaces devem respeitar com rigor os nomes e a tipagem retornados pelos endpoints REST (ex.: campos como `cpfCnpj`, `tipoPessoa`, `quantidadeCoabitantes`).

- **Estrutura de Modelos por Ciclo de Vida:**
    - **`Requisicao`:** DTO de entrada para criação (`POST`) ou atualização (`PUT`). Utiliza identificadores primitivos (números para IDs de relacionamentos, strings de código puro para enums).
    - **`Resumo`:** DTO leve para listagens e tabelas (`GET`). Relacionamentos de apoio expõem a interface `ItemRelacionadoResumo` (`{ id, nome }`) para permitir a exibição legível de designações diretamente na grelha.
    - **`Detalhe`:** DTO completo para visualização e edição detalhada (`GET /{id}`).
    - **`ConsultaParametros` / `Filtros`:** tipagem estrita de parâmetros de paginação e filtros por campo enviados via query params.

- **Enums de Domínio e Constantes de Opções:**
    - Para enums com comportamento estruturado, mantém-se a tipagem dupla: o código puro do backend (`*Codigo`) e o item descritivo formatado (`*Item` estendendo `ItemDominio<T>`).
    - As listas de opções pré-definidas para seleção em formulários são exportadas diretamente no arquivo do modelo como constantes de leitura (ex.: `SEXO_OPCOES`, `ESCOLARIDADE_OPCOES`, `RENDA_FAMILIAR_OPCOES`, `UF_OPCOES`).

- **Modelos Genéricos Compartilhados:** arquivos transversais centralizados em `@features/shared/models/`:
    - `RespostaPaginada<T>`: metadados do Spring Data (`paginaAtual`, `tamanhoPagina`, `totalElementos`, `totalPaginas`) e lista `dados`.
    - `ParametrosConsulta`: estrutura padrão de paginação (`page`, `size`, `sort`, `incluirInativos`, `filtro`).
    - `ItemDominio<T>`: par `{ codigo, descricao }` para serialização rica de enums.
    - `ErroApi`: estrutura RFC 7807 (Problem Details) e lista `ErroCampo`.

### Arquivos de Referência

| Tipo | Arquivo |
| --- | --- |
| Contrato complexo com enums, endereço, filtros e relacionamentos | `src/app/features/pessoas/models/pessoa.model.ts` |
| Contratos de tabelas de apoio e sub-recursos | `src/app/features/pessoas/models/categoria.model.ts` e `comorbidade.model.ts` |
| Metadados de paginação e consulta da API | `src/app/features/shared/models/resposta-paginada.model.ts` e `parametros-consulta.model.ts` |
| Enums de domínio e Problem Details (RFC 7807) | `src/app/features/shared/models/item-dominio.model.ts` e `erro-api.model.ts` |

---

## Camadas da Aplicação: Services

Os serviços são responsáveis pela comunicação HTTP com a API REST, encapsulando a montagem de parâmetros de consulta (`HttpParams`) e tipando as requisições e respostas com os DTOs do domínio.

### Diretrizes de Implementação

- **Consumo Direto da API REST:** toda a integração deve ser apontada diretamente para os endpoints da API backend através de `HttpClient`. O uso de simulações com mocks locais é estritamente temporário e não deve substituir os fluxos reais do servidor.

- **Injeção de Dependências:** utilize estritamente a injeção funcional declarativa no corpo da classe via `inject(HttpClient)`, sem construtor.

- **Centralização de Endpoints:** declare os caminhos dos recursos como constantes privadas no topo da classe (ex.: `private readonly endpoint = 'pessoas'`).

- **Montagem e Sanitização de Parâmetros (`HttpParams`):**
    - Toda a paginação (`page`, `size`) e ordenação (`sort`) deve ser propagada via `HttpParams`.
    - Filtros textuais devem ser sanitizados com `.trim()` antes da inclusão nos parâmetros.
    - Valores nulos, indefinidos ou vazios não devem ser anexados ao `HttpParams`.

- **Endpoints Auxiliares (`/todas`):** o método `listarTodas(incluirInativos)` deve ser implementado exclusivamente em serviços de tabelas de apoio de baixo volume (como `CategoriaService` e `ComorbidadeService`) para abastecer seletores em formulários. Entidades de grande volume (ex.: `Pessoa`) operam estritamente com o método paginado `listar(...)`.

- **Ciclo de Vida Lógico:** operações de soft delete devem utilizar `PATCH /{endpoint}/{id}/desativar` e `PATCH /{endpoint}/{id}/reativar` com payload nulo (`null`).

### Tabela de Padronização de Métodos HTTP

| Método no Service | Rota HTTP | Verbo | Retorno Observable |
| --- | --- | --- | --- |
| `listar(parametros)` | `/{endpoint}` | `GET` | `Observable<RespostaPaginada<Resumo>>` |
| `listarTodas(incluirInativos)` | `/{endpoint}/todas` | `GET` | `Observable<Resumo[]>` |
| `buscarPorId(id)` | `/{endpoint}/{id}` | `GET` | `Observable<Detalhe>` |
| `cadastrar(requisicao)` | `/{endpoint}` | `POST` | `Observable<Detalhe>` |
| `atualizar(id, requisicao)` | `/{endpoint}/{id}` | `PUT` | `Observable<Detalhe>` |
| `desativar(id)` | `/{endpoint}/{id}/desativar` | `PATCH` | `Observable<void>` |
| `reativar(id)` | `/{endpoint}/{id}/reativar` | `PATCH` | `Observable<void>` |
| `excluir(id)` | `/{endpoint}/{id}` | `DELETE` | `Observable<void>` |

### Arquivos de Referência

| Tipo | Arquivo |
| --- | --- |
| CRUD complexo com paginação extensa e múltiplos filtros | `src/app/features/pessoas/services/pessoa.service.ts` |
| CRUD padrão de apoio com suporte a `listarTodas()` | `src/app/features/pessoas/services/categoria.service.ts` e `comorbidade.service.ts` |
| Serviço de consulta de integração externa (CEP) | `src/app/features/pessoas/services/cep.service.ts` |

---

## Padrão de Listagens, Tabelas e Filtros

A listagem adota a divisão **Smart/Dumb Components** orientada pela **URL como Fonte da Verdade**:

- **URL State (`QueryParams`):** paginação, ordenação e filtros dinâmicos ficam rigorosamente sincronizados nos parâmetros da rota ativa.

- **Sanitização de Filtros Vazios:** filtros em branco ou limpos devem ser transmitidos como `null` ao `router.navigate` para que o Angular remova a chave da URL (evitando parâmetros poluídos como `?nome=&cpf=`).

- **Preservação de Contexto:** ao navegar para cadastro, edição ou visualização, propague `queryParams: this.route.snapshot.queryParams` para assegurar que a listagem seja restaurada na exata página, ordenação e filtros aplicados previamente.

- **Shell Reutilizável de Gerenciamento (`PaginaGerenciamentoComponent`):** centraliza o layout das listagens (título, ícone, botão principal de ação e botão de filtros com badge de contagem), projetando a tabela no corpo via `<ng-content />`.

- **Smart Component (`*-lista-page`):** escuta `route.queryParams`, alimenta a pipeline reativa (`switchMap`) via `toSignal()`, abre o diálogo de filtros e orquestra confirmações de ação com `DialogoConfirmacaoService`.

- **Dumb Components (`*-tabela` e `*-filtro-dialog`):** não injetam serviços HTTP e comunicam intenções através de `output()`.

### Diretrizes de Tabelas e Filtros

- **Tabelas (`*-tabela`):**
    - Recebem coleções e metadados via `input.required<T>()` e o parâmetro de ordenação via `ordenacao = input<string>('nome,asc')`.
    - Decompõem a string de ordenação em `campoOrdenado` e `direcaoOrdenada` para sincronizar `[matSortActive]` e `[matSortDirection]` com `matSortDisableClear`.
    - Utilizam a barra de paginação customizada do Design System (`.pessoa-tabela__paginacao`) com botões numéricos e setas de avanço/recuo emitindo `PageEvent`.
    - Tabelas extensas devem adotar rolagem horizontal via método `(wheel)="aoRolarTabela($event)"`.

- **Diálogos de Filtro (`*-filtro-dialog`):**
    - Ficheiros TypeScript e HTML estritamente separados (proibido o uso de templates inline).
    - Container modal envolto pela classe `.pagina-formulario`.
    - Injeção de `MAT_DIALOG_DATA` tipado para pré-carregar os filtros ativos e fechamento higienizado com `dialogRef.close(...)`.

### Arquivos de Referência

| Tipo | Arquivo |
| --- | --- |
| Componente Casca de Layout para Listagens | `src/app/features/shared/components/pagina-gerenciamento/` |
| Página Inteligente de Listagem (Smart Component) | `src/app/features/pessoas/pages/pessoa-lista-page/` e `categoria-lista-page/` |
| Componentes Dumb de Tabela | `src/app/features/pessoas/components/pessoa-tabela/` e `categoria-tabela/` |
| Componentes Dumb de Filtro Modal | `src/app/features/pessoas/components/categoria-filtro-dialog/` e `comorbidade-filtro-dialog/` |

---

## Formulários Unificados (`*-cadastro-page`)

Para eliminar duplicidade de telas e templates, as operações de **Criação (`/adicionar`)**, **Visualização (`/:id`)** e **Edição (`/:id/editar`)** compartilham o mesmo componente unificado com o sufixo padronizado `*-cadastro-page.component`.

### Diretrizes de Implementação

- **Determinação Reativa do Modo de Operação:**
    - **Visualização:** identificada pelo input `visualizacao = input(false)`, preenchido via `data: { visualizacao: true }` configurado na rota `:id`. Nesse modo, invoca-se `form.disable({ emitEvent: false })` para congelar interações.
    - **Edição:** identificada quando há `id` presente e `visualizacao()` for falso (`computed(() => !!this.id() && !this.visualizacao())`).
    - **Criação:** identificada quando não há `id` presente.

- **Formulários Tipados com `NonNullableFormBuilder`:**
    - Utilize `NonNullableFormBuilder` injetado via `inject()`.
    - Para formulários dinâmicos com alternância entre PF e PJ (ex.: `PessoaCadastroPageComponent`), escute o controle `ehPessoaJuridica` via `valueChanges` para reconfigurar validadores de CPF/CNPJ e limpar/reativar campos exclusivos de forma dinâmica.

- **Consultas e Máscaras de Entrada:**
    - Aplique máscaras de formatação visual em eventos de `(input)` e `(blur)` (para CPF/CNPJ, Telefone, CEP e Data de Nascimento), higienizando com `somenteDigitos(valor)` para envio numérico puro à API.
    - Integração de CEP: o preenchimento de 8 dígitos numéricos no campo aciona `consultarCep` via `PessoaService`, preenchendo logradouro, bairro, cidade e UF automaticamente com indicador de progresso sem interromper a digitação.

- **Tratamento de Enums e Relacionamentos:**
    - *Dropdowns* consomem diretamente as constantes de opções (`SEXO_OPCOES`, `ESCOLARIDADE_OPCOES`, etc.) ou a lista de entidades ativas (`comorbidades()`, `categorias()`).
    - Na carga detalhada (`patchValue`), extraia os identificadores numéricos ou códigos primitivos (ex.: `c.id` para seletores múltiplos).

- **Mapeamento de Erros de Submissão (RFC 7807):**
    - Erros de validação HTTP 400 (`erros`) são associados diretamente aos controles do formulário via `form.get(campo)?.setErrors({ backend: mensagem })`.
    - Erros de duplicidade HTTP 409 marcam os campos fiscais com erro específico e acionam `notificacaoService.alerta(...)`.

- **Navegação com Preservação de Histórico:**
    - Ao concluir com sucesso uma edição ou ao acionar o botão "Voltar" / "Cancelar", utilize `Location.back()` para restaurar a listagem na exata página, ordenação e filtros previamente aplicados.

### Arquivos de Referência

| Tipo | Arquivo |
| --- | --- |
| Cadastro complexo com PF/PJ, CEP, máscaras, enums e relacionamentos | `src/app/features/pessoas/pages/pessoa-cadastro-page/` |
| Cadastro padrão de apoio com soft delete e exclusão física | `src/app/features/pessoas/pages/comorbidade-cadastro-page/` e `categoria-cadastro-page/` |

---

## Utilitários Globais (`shared/` e `core/`)

Componentes transversais fornecem notificações, caixas de diálogo de confirmação e interceptação HTTP para toda a aplicação.

### Diretrizes de Uso

- **Notificações Globais (`NotificacaoService`):** abstrai o `MatSnackBar` do Angular Material, expondo métodos simples `sucesso()`, `alerta()` e `erro()` com tempos e posições padronizadas para feedbacks imediatos.

- **Diálogo de Confirmação (`DialogoConfirmacaoService`):** executa confirmações assíncronas através de `firstValueFrom` retornando uma `Promise<boolean>`, utilizado antes de operações críticas como desativação, reativação ou exclusão.

- **Interceptador de API (`apiInterceptorFn`):** prefixação dinâmica de rotas relativas com a URL base declarada em `environment.apiUrl`.

- **Interceptador de Erros (`erroInterceptorFn`):** interceptação centralizada de respostas HTTP não sucedidas, traduzindo payloads Problem Details (RFC 7807) para mensagens de toast e gerenciando redirecionamentos de sessão.

### Arquivos de Referência

| Tipo | Arquivo |
| --- | --- |
| Serviço de Mensagens e Notificações (Toast) | `src/app/features/shared/services/notificacao.service.ts` |
| Diálogo Modal Genérico de Confirmação | `src/app/features/shared/services/dialogo-confirmacao.service.ts` |
| Componente Template de Confirmação | `src/app/features/shared/components/dialogo-confirmacao.component.ts` e `.html` |
| Interceptor de URL Base da API | `src/app/core/interceptors/api.interceptor.ts` |
| Interceptor Global de Falhas HTTP | `src/app/core/interceptors/erro.interceptor.ts` |
