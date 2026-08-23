# Padrão de Arquitetura do Frontend

# Importate!

Realize adições e modificações no frontend conforme os contratos de API na pasta docs/.

# Visão Geral

Este projeto utiliza **Angular (v17+),** então deve-se priorizar adotar práticas a partir dessa versão.

---

### Diretrizes de Implementação:

- **Arquitetura 100% Standalone:**
    - O uso de `NgModule` é estritamente proibido. Todos os componentes, diretivas e pipes devem declarar `standalone: true` (ou omitir caso o Angular 17+ já assuma por padrão) e importar apenas o que utilizam em seu array `imports: []`.
- **Sintaxe de fluxo de controle:**
    - Não utilize diretivas estruturais legadas (`ngIf`, `ngFor`, `ngSwitch`).
    - Utilize exclusivamente a sintaxe de blocos nativa: `@if`, `@else`, `@switch`, `@case` e `@for`.
    - Todo `@for` deve obrigatoriamente declarar a propriedade `track` com um identificador único (ex.: `@for (item of itens(); track item.id)`), acompanhado do bloco `@empty` para listas sem registros.
- **Reatividade com Signals:**
    - Gerencie estados locais e valores mutáveis usando `signal()`.
    - Utilize `computed()` para estados derivados/calculados.
    - Substitua `@Input()` e `@Output()` pelas novas funções `input()`, `input.required()` e `output()`.
- **Injeção de Dependências com `inject()`:**
    - Não utilize injeção via construtor. Declare as dependências diretamente no corpo da classe:

        ```tsx
        private readonly pessoaService = inject(PessoaService);
        private readonly router = inject(Router);
        ```

- **Estrutura de Arquivos de Componentes:**
    - Formulários, tabelas e páginas devem manter seus arquivos HTML (`.html`) e TypeScript (`.ts`) separados para facilitar a manutenção e legibilidade.
    - Dessa forma, ao criar um componente, sempre utilize uma pasta para englobar o `html` e o `.ts`
    - *Inline templates* (`template: \`...\``) são permitidos exclusivamente em componentes puramente visuais e enxutos com menos de 20 linhas de template.
    - Seguir o padrão de nomenclatura do Angular kebab-case com sufixo `.component`, `.page`, .`interceptor`, etc.
    - Para o nome dos selectors, utilizar o prefixo `app-`  (ex.: `app-pessoa-form)`

# Estrutura de Diretórios

- Da mesma forma do backend, o padrão é feature-based:

```json
src/
└── app/
    ├── core/
    │   ├── interceptors/
    │   ├── layout/
    │   │   ├── header.component.html
    │   │   ├── header.component.ts
    │   │   ├── sidebar.component.html
    │   │   ├── sidebar.component.ts
    │   │   ├── footer.component.html
    │   │   ├── footer.component.ts
    │   └── services/
    │
    ├── features/
    │   ├── pessoas/
    │   │   ├── components/
    │   │   ├── models/
    │   │   ├── pages/
    │   │   ├── services/
    │   │
    │   ├── capacitacoes/
    │   │
    │   ├── doacoes/
    │   │
    │   └── relatorios/
    │
    ├── shared/
    │   ├── components/
    │   ├── models/
    │   │   ├── erro-api.model.ts
    │   └── pipes/
    │
    ├── app.config.ts
    ├── app.routes.ts
    └── app.component.ts
```

# Estilização

O projeto utiliza a combinação de **Tailwind CSS v4** para layout/utilitários e **Angular Material** para componentes estruturais de interface.

## Diretrizes de Implementação

- **Zero arquivos `.css` / `.scss` por componente:** Não crie arquivos de estilo locais para cada componente. Use as classes utilitárias do Tailwind diretamente no HTML. Crie apenas em casos mais específicos.
- **Foco no Tailwind:** Espaçamentos (`p-*`, `m-*`), dimensões (`w-*`, `h-*`), alinhamentos (`flex`, `grid`), tipografia utilitária e cores de layout devem ser feitos 100% via classes Tailwind.
- Utilize os componentes em `core/layout` para criar o cabeçalho e rodapé

### Localização das configurações

| **Finalidade** | **Arquivo / Diretório** | **Descrição** |
| --- | --- | --- |
| **Variáveis e Design Tokens do TailwindTokens e Tema do Tailwind v4**  | `src/styles.scss` | Importação (@import "tailwindcss";) e definição de tokens/cores via bloco @theme { ... }. |
| **Tema Global do Angular Material** | `src/styles/material-theme.scss` | Definição da paleta de cores primária, secundária e tipografia oficial do Material Design. |
| **Estilos Globais e Reset** | `src/styles.scss` | Importação das diretivas do Tailwind (`@tailwind base;...`), fontes globais e estilos do body/html. |

### Responsabilidade de Tailwind v4 e Angular Material

- Deixe o Angular Material cuidar da estrutura interna, animações de foco, acessibilidade e estados (`mat-form-field`, `mat-select`, `mat-table`, `mat-dialog`).
- Use Tailwind para estruturar os grids do formulário, espaçamentos entre campos e larguras máximas.

#### Exemplo de Uso Correto:

```tsx
<!-- Grid responsivo construído com classes Tailwind, usando componentes Material -->
<div class="grid grid-cols-1 md:grid-cols-2 gap-4 p-6 bg-white rounded-lg shadow-sm">

  <mat-form-field appearance="outline" class="w-full">
    <mat-label>Nome Completo</mat-label>
    <input matInput formControlName="nome" placeholder="Digite o nome" />
  </mat-form-field>

  <mat-form-field appearance="outline" class="w-full">
    <mat-label>CPF</mat-label>
    <input matInput formControlName="cpf" placeholder="000.000.000-00" />
  </mat-form-field>

</div>
```

# Roteamento

O roteamento da aplicação utiliza um arquivo principal que aponta pros arquivos de rota por módulo funcional.

## Diretrizes de Implementação

- Nas rotas, proíba o uso de classes implementando `CanActivate`.Utilize exclusivamente a sintaxe funcional baseada em `CanActivateFn`
- Nunca importe classes de páginas diretamente no topo do arquivo de rotas com `import { MinhaPage } from ...`. Utilize sempre `loadChildren` ou `loadComponent` com `import(...)`.

### Modelo e exemplo de rotas

O arquivo raiz apenas orquestra o layout base e as guards e delega as rotas filhas para cada módulo funcional via `loadChildren`:

```tsx

export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/pages/login-page/login-page.component').then(
        (m) => m.LoginPageComponent
      )
  },

  {
    path: '',
    component: LayoutComponent,
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'pessoas'
      },
      {
        path: 'pessoas',
        loadChildren: () =>
          import('./features/pessoas/pessoas.routes').then((m) => m.PESSOAS_ROUTES)
      },
      {
        path: 'capacitacoes',
        loadChildren: () =>
          import('./features/capacitacoes/capacitacoes.routes').then((m) => m.CAPACITACOES_ROUTES)
      },
      {
        path: 'doacoes',
        loadChildren: () =>
          import('./features/doacoes/doacoes.routes').then((m) => m.DOACOES_ROUTES)
      },
      {
        path: 'relatorios',
        loadChildren: () =>
          import('./features/relatorios/relatorios.routes').then((m) => m.RELATORIOS_ROUTES)
      }
    ]
  },

  // Rota coringa para redirecionamento
  {
    path: '**',
    redirectTo: 'pessoas'
  }
];
```

Cada módulo gerencia suas próprias páginas e componentes de rota interna de forma isolada, a seguir um exemplo:

```tsx
import { Routes } from '@angular/router';

export const PESSOAS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/pessoa-lista-page/pessoa-lista-page.component').then(
        (m) => m.PessoaListPageComponent
      )
  },
  {
    path: 'novo',
    loadComponent: () =>
      import('./pages/pessoa-cadastro-page/pessoa-cadastro-page.component').then(
        (m) => m.PessoaCadastroPageComponent
      )
  },
  {
    path: 'categorias',
    loadComponent: () =>
      import('./pages/categoria-lista-page/categoria-lista-page.component').then(
        (m) => m.CategoriaListaPageComponent
      )
  },
  {
    path: 'comorbidades',
    loadComponent: () =>
      import('./pages/comorbidade-lista-page/comorbidade-lista-page.component').then(
        (m) => m.ComorbidadeListaPageComponent
      )
  }
];
```

# Camadas da Aplicação

## Models / Interfaces

Os models terão o mesmo tipo dos DTOs do backend.

### Diretrizes de implementação

- O backend utiliza Jackson com `@JsonFormat(shape = OBJECT)` e `@JsonCreator` nos Enums Java (`Sexo`, `Escolaridade`, `RendaFamiliar`). Isso estabelece uma regra fixa de transporte:
    - **Leitura (GET):** A API retorna um objeto estruturado `{ "codigo": "...", "descricao": "..." }`.
    - **Envio (POST / PUT):** A API recebe a String simples do código (`"FEMININO"`, `"MASCULINO"`).
- Datas devem ser strings
- Todo atributo que no backend não possui `@NotBlank` ou `@NotNull` deve ser declarado como opcional com `?`.
- Mantenha os mesmos nomes de atributos definidos nos contratos de API

#### Interface Genérica de Domínio (`src/app/shared/models/item-dominio.model.ts`)

```tsx
export interface ItemDominio<T = string> {
  codigo: T;
  descricao: string;
}
```

#### Definições de Domínio do Módulo (`src/app/features/pessoas/models/pessoa.model.ts`)

Defina um enum com padrão de nomenclatura `ElementoCodigo`  e crie o type `ItemDominio`

```tsx
export enum SexoCodigo {
  FEMININO = 'FEMININO',
  MASCULINO = 'MASCULINO',
  PREFIRO_NAO_INFORMAR = 'PREFIRO_NAO_INFORMAR'
}

export type SexoItem = ItemDominio<SexoCodigo>;

export enum EscolaridadeCodigo {
  FUNDAMENTAL_INCOMPLETO = 'FUNDAMENTAL_INCOMPLETO',
  FUNDAMENTAL_COMPLETO = 'FUNDAMENTAL_COMPLETO',
  ENSINO_MEDIO_INCOMPLETO = 'ENSINO_MEDIO_INCOMPLETO',
  ENSINO_MEDIO_COMPLETO = 'ENSINO_MEDIO_COMPLETO',
  SUPERIOR_INCOMPLETO = 'SUPERIOR_INCOMPLETO',
  SUPERIOR_COMPLETO = 'SUPERIOR_COMPLETO'
}

export type EscolaridadeItem = ItemDominio<EscolaridadeCodigo>;
```

### Interfaces Espelhando os DTOs de Pessoas

Seguindo o padrão de segregação de DTOs do backend, crie as interfaces num mesmo arquivo (ex.: pessoa.model.ts) e copie o nome delas do backend. Os nomes comuns serão:

1. `Requisicao`
2. `Atualizacao`
3. `Resumo`
4. `Detalhe`

```tsx
// src/app/features/pessoas/models/pessoa.model.ts

export interface PessoaResumo {
}

export interface PessoaDetalhe {
}
export interface PessoaRequisicao {
}
```

Para saber mais, leia a parte de DTO do arquivo de padronização da API

A seguir, modelos globais em shared que também estão no backend:

#### Paginação da API (`src/app/shared/models/paginacao.model.ts`)

```tsx
export interface RespostaPaginada<T> {
  dados: T[];
  paginaAtual: number;
  tamanhoPagina: number;
  totalElementos: number;
  totalPaginas: number;
}
```

#### Tratamento de Erros RFC 7807 (`src/app/shared/models/erro-api.model.ts`)

```tsx
export interface ErroCampo {
  campo: string;
  mensagem: string;
}

export interface ErroApi {
  type: string;
  title: string;
  status: number;
  detail: string;
  instance: string;
  timestamp: string;
  erros?: ErroCampo[];
}
```

## Services

A camada de Service no Frontend é responsável exclusivamente pela comunicação HTTP com a API REST, manipulação de query parameters e tipagem estrita das requisições e respostas. Ela desacopla as páginas e componentes (*Smart* e *Dumb*) dos detalhes de infraestrutura de rede.

### Diretrizes de Implementação

- **Sem `try/catch` ou tratamento manual de erros HTTP:** Erros globais (como `401`, `403` ou `500`) são capturados pelo Interceptor global.
- **Suporte à Paginação e Filtros:** Parâmetros de busca, paginação (`page`, `size`, `sort`) e filtros booleanos (`incluirInativos`) devem ser transmitidos via `HttpParams`.
- **Nomenclatura Padronizada dos Métodos:**
    - `cadastrar(...)`
    - `listar(...)`
    - `buscarPorId(...)`
    - `atualizar(...)`
    - `desativar(...)`
    - `reativar(...)`

### Mapeamento de Métodos e Endpoints

| **Método no Service** | **Rota HTTP** | **Verbo HTTP** | **Parâmetros de Entrada** | **Retorno (Observable<T>)** |
| --- | --- | --- | --- | --- |
| `cadastrar` | `/pessoas` | `POST` | `PessoaRequisicao` | `Observable<PessoaDetalhe>` |
| `listar` | `/pessoas` | `GET` | `page`, `size`, `sort`, `incluirInativos` | `Observable<RespostaPaginada<PessoaResumo>>` |
| `buscarPorId` | `/pessoas/{id}` | `GET` | `id: number` | `Observable<PessoaDetalhe>` |
| `atualizar` | `/pessoas/{id}` | `PUT` | `id: number`, `PessoaRequisicao` | `Observable<PessoaDetalhe>` |
| `desativar` | `/pessoas/{id}/desativar` | `PATCH` | `id: number` | `Observable<void>` |
| `reativar` | `/pessoas/{id}/reativar` | `PATCH` | `id: number` | `Observable<void>` |

### Exemplo de Estrutura de Classe Service (`pessoa.service.ts`)

TypeScript

```tsx
export class PessoaService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = 'pessoas';

  cadastrar(requisicao: PessoaRequisicao): Observable<PessoaDetalhe> {
    return this.http.post<PessoaDetalhe>(this.endpoint, requisicao);
  }

  listar(...): Observable<RespostaPaginada<PessoaResumo>> {
    // ...
    return this.http.get<RespostaPaginada<PessoaResumo>>(this.endpoint, { params });
  }

  buscarPorId(id: number): Observable<PessoaDetalhe> {
    return this.http.get<PessoaDetalhe>(`${this.endpoint}/${id}`);
  }

  atualizar(id: number, requisicao: PessoaRequisicao): Observable<PessoaDetalhe> {
    return this.http.put<PessoaDetalhe>(`${this.endpoint}/${id}`, requisicao);
  }

  desativar(id: number): Observable<void> {
    return this.http.patch<void>(`${this.endpoint}/${id}/desativar`, null);
  }

  reativar(id: number): Observable<void> {
    return this.http.patch<void>(`${this.endpoint}/${id}/reativar`, null);
  }
}
```

## Pages e Components

### Diretrizes de Implementação

- Priorizar definir pages como smart e os seus componentes como dumb
- Pages
    - Injetar serviços (`Service`, `Router`, `MatSnackBar`, etc.) via `inject()`.
    - Gerenciar os estados globais/locais da página utilizando `signal()` e `computed()`.
    - Disparar chamadas HTTP (buscar dados, salvar, deletar).
    - Repassar dados aos componentes filhos e escutar seus eventos de saída (`output()`).
    - Para eventos recebidos, utilizar o padrão de nomenclatura de ação direta no infinitivo + elemento modificado (`salvarPessoa()`, `excluirPessoa()`, `mudarPagina()`).
    - Para padrão de nomenclatura de nomes dos arquivos, use `elemento-cadastro/listagem/informacao-page.component`
- Components
    - Receber dados exclusivamente via `input()` ou `input.required()`.
    - Emitir intenções do usuário exclusivamente via `output()` (ex.: `aoSalvar`, `aoExcluir`).
    - Não injetam serviços de API (`HttpClient` ou serviços de negócio).
    - Podem injetar apenas utilitários puros de interface (ex.: classes de formatação ou helpers locais).
    - Para output, utilizar o padrão de nomenclatura "ao" + ação no infinitivo (`aoSalvar`, `aoExcluir`, `aoMudarPagina`).
    - Para os nomes, siga a convenção de elemento-ação `elemento-formulario/tabela/informacao/filtro/select.component`

# Dados Simulados (Mocks de API)

Durante o desenvolvimento inicial de telas e fluxos em que o backend ainda não disponibilizou os endpoints, utilize dados falsos tipados de forma isolada. O padrão adotado é o **retorno direto via RxJS nos Services**, simulando a assincronicidade com `of()` e `delay()`, permitindo fácil transição para a API real sem alterar a assinatura dos métodos ou o código das páginas.

---

## Diretrizes de Implementação

* **Localização dos arquivos de mock:** Crie uma pasta `mocks/` dentro da respectiva feature (ex.: `src/app/features/pessoas/mocks/pessoa.mock.ts`).
* **Tipagem estrita:** Os mocks devem implementar rigorosamente as mesmas interfaces de DTO (`Resumo`, `Detalhe`, `RespostaPaginada<T>`), incluindo o formato estruturado de Enums para leitura (`{ codigo, descricao }`).
* **Simulação de latência de rede:** Sempre utilize o operador `.pipe(delay(300))` em conjunto com `of(...)`. Isso garante a correta validação dos estados de carregamento (`carregando.set(true)`) nas páginas.
* **Flag de controle de mock:** Centralize a alternância entre dados locais e a API através do `environment.mockApi` ou de uma propriedade booleana de controle no próprio Service.

---

## Estrutura de Diretórios da Feature com Mocks

```json
src/app/features/pessoas/
├── components/
├── mocks/
│   └── pessoa.mock.ts
├── models/
│   └── pessoa.model.ts
├── pages/
├── services/
│   └── pessoa.service.ts
└── pessoas.routes.ts

```

---

## Exemplo de Arquivo de Mock (`pessoa.mock.ts`)

```tsx
import { RespostaPaginada } from '../../../shared/models/paginacao.model';
import { PessoaDetalhe, PessoaResumo, SexoCodigo } from '../models/pessoa.model';

export const MOCK_PESSOAS_LISTA: RespostaPaginada<PessoaResumo> = {
  dados: [
    {
      id: 1,
      nome: 'Ana Silva',
      cpf: '123.456.789-00',
      sexo: { codigo: SexoCodigo.FEMININO, descricao: 'Feminino' },
      ativo: true
    },
    {
      id: 2,
      nome: 'Carlos Souza',
      cpf: '987.654.321-11',
      sexo: { codigo: SexoCodigo.MASCULINO, descricao: 'Masculino' },
      ativo: true
    }
  ],
  paginaAtual: 0,
  tamanhoPagina: 10,
  totalElementos: 2,
  totalPaginas: 1
};

export const MOCK_PESSOA_DETALHE: PessoaDetalhe = {
  id: 1,
  nome: 'Ana Silva',
  cpf: '123.456.789-00',
  categoria: { id: 10, nome: 'Estudante' },
  sexo: { codigo: SexoCodigo.FEMININO, descricao: 'Feminino' }
};

```

---

## Estrutura do Service com Alternância para Endpoint Real (`pessoa.service.ts`)

O Service mantém a implementação HTTP pronta. Para ativar a API real, basta alterar o valor da flag de controle:

```tsx
import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { delay } from 'rxjs/operators';
import { RespostaPaginada } from '../../../shared/models/paginacao.model';
import { MOCK_PESSOA_DETALHE, MOCK_PESSOAS_LISTA } from '../mocks/pessoa.mock';
import { PessoaDetalhe, PessoaRequisicao, PessoaResumo } from '../models/pessoa.model';

@Injectable({
  providedIn: 'root'
})
export class PessoaService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = 'pessoas';

  // Alterne para false quando o backend estiver disponível
  private readonly useMock = true;

  cadastrar(requisicao: PessoaRequisicao): Observable<PessoaDetalhe> {
    if (this.useMock) {
      const mockCriado: PessoaDetalhe = {
        ...MOCK_PESSOA_DETALHE,
        nome: requisicao.nome,
        id: Math.floor(Math.random() * 1000) + 1
      };
      return of(mockCriado).pipe(delay(400));
    }
    return this.http.post<PessoaDetalhe>(this.endpoint, requisicao);
  }

  listar(params?: HttpParams): Observable<RespostaPaginada<PessoaResumo>> {
    if (this.useMock) {
      return of(MOCK_PESSOAS_LISTA).pipe(delay(300));
    }
    return this.http.get<RespostaPaginada<PessoaResumo>>(this.endpoint, { params });
  }

  buscarPorId(id: number): Observable<PessoaDetalhe> {
    if (this.useMock) {
      return of({ ...MOCK_PESSOA_DETALHE, id }).pipe(delay(300));
    }
    return this.http.get<PessoaDetalhe>(`${this.endpoint}/${id}`);
  }

  atualizar(id: number, requisicao: PessoaRequisicao): Observable<PessoaDetalhe> {
    if (this.useMock) {
      return of({ ...MOCK_PESSOA_DETALHE, ...requisicao, id }).pipe(delay(400));
    }
    return this.http.put<PessoaDetalhe>(`${this.endpoint}/${id}`, requisicao);
  }

  desativar(id: number): Observable<void> {
    if (this.useMock) {
      return of(void 0).pipe(delay(300));
    }
    return this.http.patch<void>(`${this.endpoint}/${id}/desativar`, null);
  }

  reativar(id: number): Observable<void> {
    if (this.useMock) {
      return of(void 0).pipe(delay(300));
    }
    return this.http.patch<void>(`${this.endpoint}/${id}/reativar`, null);
  }
}

```

---

## Processo de Transição para Produção / API Real

Quando os endpoints do backend forem disponibilizados:

1. Alterne a flag `useMock` para `false` no Service correspondente (ou aponte para `environment.mockApi`).
2. Valide as chamadas reais contra o `apiInterceptor` e o `erroInterceptor`.
3. Caso os mocks não sejam mais necessários para testes unitários locais, os arquivos dentro da pasta `mocks/` podem ser mantidos para fins de testes rápidos ou removidos conforme a cobertura de testes do módulo.

# Formulários

Para simplicidade, crie a lógica dos formulários na propria page, aí não irá precisar de outputs ou tantas conversas entre componentes. Use-os tanto para cadastro quanto atualização.

Siga os exemplos a seguir para formulários

```tsx

@Component({
  selector: 'app-pessoa-cadastro-page',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule
  ],
  templateUrl: './pessoa-cadastro-page.component.html'
})
export class PessoaCadastroPageComponent {
  // Injeções
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly router = inject(Router);
  private readonly notificacaoService = inject(NotificacaoService);
  private readonly pessoaService = inject(PessoaService);
  private readonly categoriaService = inject(CategoriaService);

  // Parâmetro de Rota (Id opcional para Edição)
  readonly id = input<string>();

  // Signals de Estado
  readonly categorias = signal<CategoriaResumo[]>([]);
  readonly carregando = signal<boolean>(false);

  // Formulário Reativo (Exemplo enxuto com 2 campos)
  protected readonly form = this.fb.group({
    nome: ['', [Validators.required, Validators.minLength(3)]],
    categoriaId: [null as number | null, [Validators.required]]
  });

  get modoEdicao(): boolean {
    return !!this.id();
  }

  constructor() {
    this.carregarCategorias();

    effect(() => {
      const idRegistro = this.id();
      if (idRegistro) {
        this.carregarDadosEdicao(Number(idRegistro));
      }
    });
  }

  salvar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const payload = this.montarPayload();
    this.carregando.set(true);

    const requisicao$ = this.modoEdicao
      ? this.pessoaService.atualizar(Number(this.id()), payload)
      : this.pessoaService.cadastrar(payload);

    requisicao$.subscribe({
      next: () => {
        this.notificacaoService.sucesso(`Pessoa ${this.modoEdicao ? 'atualizada' : 'cadastrada'} com sucesso!`);
        this.voltarParaListagem();
      },
      error: (err: HttpErrorResponse) => this.tratarErroSubmissao(err),
      complete: () => this.carregando.set(false)
    });
  }

  cancelar(): void {
    this.voltarParaListagem();
  }

  private carregarCategorias(): void {
    this.categoriaService.listar().subscribe({
      next: (resposta) => this.categorias.set(resposta.dados)
    });
  }

  private carregarDadosEdicao(id: number): void {
    this.carregando.set(true);
    this.pessoaService.buscarPorId(id).subscribe({
      next: (pessoa) => this.preencherFormulario(pessoa),
      error: () => this.voltarParaListagem(),
      complete: () => this.carregando.set(false)
    });
  }

  private preencherFormulario(pessoa: PessoaDetalhe): void {
    this.form.patchValue({
      nome: pessoa.nome,
      categoriaId: pessoa.categoria.id
    });
  }

  private montarPayload(): PessoaRequisicao {
    const raw = this.form.getRawValue();
    return {
      nome: raw.nome.trim(),
      categoriaId: raw.categoriaId!
      // Outros campos obrigatórios com valores default/vazios
    } as PessoaRequisicao;
  }

  private tratarErroSubmissao(err: HttpErrorResponse): void {
    this.carregando.set(false);
    const erroApi = err.error as ErroApi | undefined;

    if (err.status === 400 && erroApi?.erros) {
      erroApi.erros.forEach((e) => {
        const control = this.form.get(e.campo);
        if (control) {
          control.setErrors({ backend: e.mensagem });
        }
      });
    }
  }

  private voltarParaListagem(): void {
    this.router.navigate(['/pessoas']);
  }
}
```

```tsx
<section class="max-w-xl mx-auto p-6 bg-white rounded-lg shadow-sm">
  <h1 class="text-2xl font-bold mb-6 text-gray-800">
    @if (modoEdicao) {
      Editar Pessoa
    } @else {
      Nova Pessoa
    }
  </h1>

  <form [formGroup]="form" (ngSubmit)="salvar()" class="space-y-4">

    <mat-form-field appearance="outline" class="w-full">
      <mat-label>Nome Completo</mat-label>
      <input matInput formControlName="nome" placeholder="Digite o nome" />
      @if (form.get('nome')?.hasError('required')) {
        <mat-error>Nome é obrigatório</mat-error>
      }
    </mat-form-field>

    <mat-form-field appearance="outline" class="w-full">
      <mat-label>Categoria</mat-label>
      <mat-select formControlName="categoriaId">
        @for (cat of categorias(); track cat.id) {
          <mat-option [value]="cat.id">{{ cat.nome }}</mat-option>
        }
      </mat-select>
      @if (form.get('categoriaId')?.hasError('required')) {
        <mat-error>Categoria é obrigatória</mat-error>
      }
    </mat-form-field>

    <div class="flex justify-end gap-3 pt-4 border-t border-gray-100">
      <button mat-button type="button" (click)="cancelar()">
        Cancelar
      </button>

      <button
        mat-raised-button
        color="primary"
        type="submit"
        [disabled]="carregando()">
        Salvar
      </button>
    </div>

  </form>
</section>
```

# Utilitários Globais e Shared (`shared/` e `core/`)

Para evitar duplicação de lógica visual e comportamental, os recursos de feedback visual (notificações), modais de confirmação e prefixação de requisições HTTP ficam centralizados em utilitários reutilizáveis.

## Serviço de Notificação / Toast (`src/app/core/services/notificacao.service.ts`)

Centraliza o uso do `MatSnackBar`, padronizando o tempo de exibição e posicionamento das mensagens rápidas de feedback.

```tsx

export interface NotificacaoOpcoes {
  duracao?: number;
  posicaoVertical?: MatSnackBarVerticalPosition;
  posicaoHorizontal?: MatSnackBarHorizontalPosition;
}

@Injectable({
  providedIn: 'root'
})
export class NotificacaoService{
  private readonly snackbar = inject(MatSnackBar);

  sucesso(mensagem: string, opcoes?: NotificacaoOpcoes): void {
    this.exibir(mensagem, 'Fechar', { ...opcoes });
  }

  alerta(mensagem: string, opcoes?: NotificacaoOpcoes): void {
    this.exibir(mensagem, 'Atenção', { ...opcoes });
  }

  erro(mensagem: string, opcoes?: NotificacaoOpcoes): void {
    this.exibir(mensagem, 'Fechar', { duracao: 5000, ...opcoes });
  }

  private exibir(mensagem: string, acao: string, opcoes?: NotificacaoOpcoes): void {
    this.snackbar.open(mensagem, acao, {
      duration: opcoes?.duracao ?? 3500,
      verticalPosition: opcoes?.posicaoVertical ?? 'top',
      horizontalPosition: opcoes?.posicaoHorizontal ?? 'center'
    });
  }
}
```

## Modal Genérico de Confirmação (`shared/components/dialogo-confirmacao/`)

Evita recriar caixas de diálogo repetitivas para ações críticas (ex.: inativar ou reativar registros).

### Componente de Confirmação (`dialogo-confirmacao.component.ts`)

```tsx

export interface OptionDialogData {
  titulo: string;
  mensagem: string;
}

@Component({
  selector: 'app-dialogo-confirmacao',
  templateUrl: 'dialogo-confirmacao.component.html',
  imports: [
    MatDialogTitle,
    MatDialogContent,
    MatDialogActions,
    MatDialogClose,
    MatButton
  ],
})
export class DialogoConfirmacaoComponent {
  readonly dialogRef = inject(MatDialogRef<DialogoConfirmacaoComponent>);
  readonly data = inject<OptionDialogData>(MAT_DIALOG_DATA);

  fechar(): void {
    this.dialogRef.close();
  }
}

```

Template do Modal (dialogo-confirmacao.component.html)

```html
<h2 mat-dialog-title class="text-lg font-semibold">{{ data.titulo }}</h2>

<mat-dialog-content>
  <p class="text-sm text-gray-600">{{ data.mensagem }}</p>
</mat-dialog-content>

<mat-dialog-actions align="end">
  <button mat-button [mat-dialog-close]="false">Cancelar</button>
  <button mat-raised-button color="warn" [mat-dialog-close]="true">Confirmar</button>
</mat-dialog-actions>
```

### Serviço Utilitário para Abertura do Modal (`dialogo-confirmacao.service.ts`)

Permite chamar a confirmação de forma assíncrona usando `async/await` direto nos componentes:

```tsx

@Injectable({
  providedIn: 'root'
})
export class DialogoConfirmacaoService{
  private readonly dialog = inject(MatDialog);

  async confirmar(titulo: string, mensagem: string): Promise<boolean> {
    const dialogRef = this.dialog.open<DialogoConfirmacaoComponent, OptionDialogoConfirmacaoData, boolean>(
      DialogoConfirmacaoComponent,
      {
        data: { titulo, mensagem },
        width: '400px'
      }
    );

    const resultado = await firstValueFrom(dialogRef.afterClosed());
    return !!resultado;
  }
}
```

### Exemplo de Uso na Page:

```tsx
async inativarRegistro(id: number): Promise<void> {
  const confirmou = await this.DialogoConfirmacao.confirmar(
    'Inativar Associado',
    'Tem certeza que deseja inativar este registro?'
  );

  if (confirmou) {
    this.pessoaService.desativar(id).subscribe(...);
  }
}
```

## Interceptor de URL Base da API (`src/app/core/http/api.interceptor.ts`)

Centraliza o prefixo base da API, dispensando a necessidade de concatenar `environment.apiUrl` manualmente em cada chamada dos services quando utilizarem rotas relativas.

```tsx

export const apiInterceptor: HttpInterceptorFn = (req, next) => {
  // Ignora requisições completas (ex.: chamadas externas para ViaCEP)
  if (req.url.startsWith('http://') || req.url.startsWith('https://')) {
    return next(req);
  }

  const requisicaoComPrefixo = req.clone({
    url: `${environment.apiUrl}/${req.url.replace(/^\//, '')}`
  });

  return next(requisicaoComPrefixo);
};
```

# Tratamento Global de Erros HTTP (RFC 7807)

O tratamento de erros é centralizado através de um **Interceptor HTTP Funcional** (`HttpInterceptorFn`). Ele captura as respostas de erro da API Spring Boot padronizadas sob a especificação **RFC 7807 (Problem Details)** e aciona os métodos semânticos do `NotificacaoService` (`alerta()`, `erro()`), dispensando blocos manuais de exibição de avisos dentro dos Services e Pages.

## Fluxo de Responsabilidades

1. **Backend (Spring Boot):** Intercepta exceções e devolve um JSON padronizado com `title`, `status`, `detail` e opcionalmente o array `erros` (para falhas de validação de formulário).
2. **`erroInterceptor`:** Captura a falha HTTP, decide o nível de feedback visual via `NotificacaoService` e repassa o erro adiante com `throwError`.
3. **Página / Componente:** Não precisa abrir `MatSnackBar` manualmente para erros genéricos; apenas escuta o erro se precisar marcar controles locais inválidos via `setErrors()`.

## Mapeamento por Código de Status HTTP

- **`Status 0` (Sem Conexão / Backend Fora):** Aciona `notificacaoService.erro(...)` informando falha de comunicação com o servidor.
- **`400 Bad Request`:**
    - Se contiver o array `erros` (validações `@Valid` do Spring), exibe o primeiro campo inválido via `notificacaoService.` **(RFC 7807)**`alerta(...)`.
    - Se for erro de regra de negócio geral, exibe o `detail` via `notificacaoService.alerta(...)`.
- **`401 Unauthorized`:** Limpa tokens/sessão local, exibe alerta de sessão expirada e redireciona para `/login`.
- **`403 Forbidden`:** Dispara `notificacaoService.erro(...)` informando ausência de permissão.
- **`404 Not Found` / `409 Conflict`:** Disparam `notificacaoService.alerta(...)` com a mensagem descritiva de `detail`.
- **`500 Internal Server Error`:** Dispara `notificacaoService.erro(...)` com a mensagem de falha interna.

## Implementação do Interceptor (`src/app/core/http/erro.interceptor.ts`)

```tsx
export const erroInterceptor: HttpInterceptorFn = (req, next) => {
  const notificacao = inject(NotificacaoService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // 1. Falha de rede ou backend indisponível
      if (error.status === 0) {
        notificacao.erro('Servidor indisponível. Verifique sua conexão.');
        return throwError(() => error);
      }

      const corpo = error.error as ErroApi | undefined;
      const detalhe = corpo?.detail || 'Ocorreu um erro ao processar sua solicitação.';

      // 2. Roteamento semântico de notificações
      switch (error.status) {
        case 400:
          if (corpo?.erros && corpo.erros.length > 0) {
            const primeiro = corpo.erros[0];
            notificacao.alerta(`${primeiro.campo}: ${primeiro.mensagem}`);
          } else {
            notificacao.alerta(detalhe);
          }
          break;

        case 401:
          localStorage.removeItem('access_token');
          notificacao.alerta('Sessão expirada. Faça login novamente.');
          router.navigate(['/login']);
          break;

        case 403:
          notificacao.erro('Você não tem permissão para realizar esta operação.');
          break;

        case 404:
        case 409:
          notificacao.alerta(detalhe);
          break;

        case 500:
        default:
          notificacao.erro(detalhe);
          break;
      }

      // Propaga o erro caso a página precise reagir (ex: destravar loading)
      return throwError(() => error);
    })
  );
};
```
