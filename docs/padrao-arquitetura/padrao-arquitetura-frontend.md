# Padrão de Arquitetura do Frontend

# Importante!

Não há arquivos estáticos de contrato de API na pasta `docs/`. Toda a integração, tipagem de DTOs, rotas e payloads deve ser consultada diretamente na interface do Swagger em homologação: [https://h.asmosul.site/api/swagger-ui.html](https://h.asmosul.site/api/swagger-ui.html).

# Visão Geral

Este projeto utiliza **Angular (v17+)**, priorizando práticas modernas da plataforma.

### Diretrizes de Implementação:

* **Arquitetura 100% Standalone:** O uso de `NgModule` é estritamente proibido. Todos os componentes, diretivas e pipes devem declarar `standalone: true` e importar apenas o que utilizam em seu array `imports: []`.
* **Sintaxe de Fluxo de Controle:**
* Não utilize diretivas estruturais legadas (`*ngIf`, `*ngFor`, `*ngSwitch`).
* Utilize exclusivamente a sintaxe de blocos nativa: `@if`, `@else`, `@switch`, `@case` e `@for`.
* Todo `@for` deve obrigatoriamente declarar a propriedade `track` com um identificador único (ex.: `@for (item of itens(); track item.id)`), acompanhado do bloco `@empty` para listas vazias.


* **Reatividade com Signals:**
* Gerencie estados locais e valores mutáveis usando `signal()`.
* Utilize `computed()` para estados derivados/calculados.
* Substitua `@Input()` e `@Output()` pelas funções `input()`, `input.required()` e `output()`.


* **Injeção de Dependências com `inject()`:** Não utilize injeção via construtor. Declare as dependências diretamente no corpo da classe via `inject(...)`.
* **Estrutura de Arquivos:** Formulários, tabelas e páginas devem manter seus arquivos HTML (`.html`) e TypeScript (`.ts`) separados. Mantenha os seletores padronizados com o prefixo `app-` em *kebab-case*.

# Configuração Global da Aplicação (`src/app/app.config.ts`)

A aplicação requer provedores funcionais padronizados para roteamento com binding de parâmetros, interceptores HTTP e animações:

```typescript
import { ApplicationConfig } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';

import { routes } from './app.routes';
import { apiInterceptor } from './core/interceptors/api.interceptor';
import { erroInterceptor } from './core/interceptors/erro.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    // Obrigatório: withComponentInputBinding mapeia rota :id e route.data diretamente para inputs
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withInterceptors([apiInterceptor, erroInterceptor])),
    provideAnimationsAsync()
  ]
};

```

# Estrutura de Diretórios

```text
src/
├── app/
│   ├── app.config.ts
│   ├── app.html
│   ├── app.routes.ts
│   ├── app.ts
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
│       │   │   ├── pessoa-tabela/
│       │   │   └── pessoa-filtro-dialog/
│       │   ├── models/
│       │   │   ├── categoria.model.ts
│       │   │   ├── comorbidade.model.ts
│       │   │   └── pessoa.model.ts
│       │   ├── pages/
│       │   │   ├── pessoa-lista-page/
│       │   │   ├── pessoa-form-page/
│       │   │   ├── categoria-form-page/
│       │   │   └── comorbidade-form-page/
│       │   ├── services/
│       │   │   ├── categoria.service.ts
│       │   │   ├── comorbidade.service.ts
│       │   │   └── pessoa.service.ts
│       │   └── pessoas.routes.ts
│       └── shared/
│           ├── components/
│           │   ├── dialogo-confirmacao.component.html
│           │   └── dialogo-confirmacao.component.ts
│           ├── models/
│           │   ├── erro-api.model.ts
│           │   ├── item-dominio.model.ts
│           │   ├── paginacao.model.ts
│           │   └── parametros-paginacao.model.ts
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
└── styles.scss

```

# Estilização

O projeto utiliza **Tailwind CSS v4** para diagramação/layout e **Angular Material** com design tokens customizados globalmente para aproximar a interface do Figma.

## Diretrizes de Implementação

* **Zero arquivos `.css` / `.scss` por componente:** É proibido criar arquivos de estilo locais ou blocos `styles: \`...`` dentro de páginas ou componentes. Todos os ajustes visuais do Design System (Figma) residem exclusivamente nos arquivos de configuração global.
* **Padronização Global de Inputs e Botões (Design Tokens do Figma):**
* Campos de formulário com altura de `48px`, cantos arredondados de `8px` e contornos alinhados à paleta corporativa.
* Botões de ação com altura de `44px`, cantos de `8px` e largura mínima de `10rem` em telas maiores que mobile.


* **Responsabilidade Visual:**
* O Angular Material gerencia acessibilidade, foco e componentes estruturais (`mat-table`, `mat-form-field`, `mat-select`, `mat-dialog`, `mat-paginator`).
* O Tailwind CSS organiza grids responsivos, margens, paddings, alinhamentos e tipografia com as cores institucionais.



### Localização das Configurações Globais

| **Finalidade** | **Arquivo / Diretório** | **Descrição** |
| --- | --- | --- |
| **Tokens do Tailwind v4 e Reset** | `src/styles.scss` | Importação (`@import "tailwindcss";`), definição das cores da marca no bloco `@theme { ... }` e cores globais para títulos (`h1`, `h2`, `h3`). |
| **Design Tokens Globais do Material** | `src/styles/material-theme.scss` | Configuração das variáveis CSS públicas do Angular Material (`--mat-form-field-*`, `--mat-button-*`) para aplicar o tema do Figma em todos os formulários. |

### Configuração de Tema do Angular Material (`src/styles/material-theme.scss`)

```scss
:root {
  // Paleta Institucional (Figma)
  --asmosul-navy: #14395c;
  --asmosul-surface: #e9eef4;
  --asmosul-border: #d9dee5;

  // Tokens Públicos do Angular Material Form Field (Inputs e Selects)
  --mat-form-field-container-height: 48px;
  --mat-form-field-container-vertical-padding: 12px;
  --mat-form-field-outlined-container-shape: 8px;
  --mat-form-field-outlined-outline-color: var(--asmosul-border);
  --mat-form-field-outlined-hover-outline-color: var(--asmosul-navy);
  --mat-form-field-outlined-focus-outline-color: var(--asmosul-navy);

  // Tokens Públicos de Botões do Angular Material
  --mat-button-filled-container-shape: 8px;
  --mat-button-filled-container-height: 44px;
}

// Botões de Ação do Sistema
button[type='submit'] {
  --mat-button-filled-container-color: var(--asmosul-navy);
  --mat-button-filled-label-text-color: #ffffff;
}

button[type='button'] {
  --mat-button-filled-container-color: var(--asmosul-surface);
  --mat-button-filled-label-text-color: var(--asmosul-navy);
}

@media (min-width: 640px) {
  button[mat-flat-button],
  button[mat-raised-button] {
    min-width: 10rem;
  }
}

```

### Configuração do Tailwind CSS v4 (`src/styles.scss`)

```scss
@import "tailwindcss";

@theme {
  --color-asmosul-navy: #14395c;
  --color-asmosul-surface: #e9eef4;
  --color-asmosul-border: #d9dee5;
}

h1, h2, h3 {
  color: var(--asmosul-navy);
}

```

# Roteamento

## Diretrizes de Implementação

* **Centralização por Feature:** Toda entidade pertencente ao mesmo domínio (ex.: Pessoas, Categorias, Comorbidades) centraliza suas rotas em um único arquivo por módulo (`pessoas.routes.ts`).
* **Padrão Semântico de Rotas (CRUD):**
* **Listagem:** `''` (raiz do sub-recurso)
* **Criação:** `'adicionar'`
* **Visualização:** `':id'` (reaproveita o formulário com flag `visualizacao: true` via `data`)
* **Edição:** `':id/editar'` (reaproveita o formulário com `visualizacao: false`)


* **Reaproveitamento de Componente de Formulário:** As rotas de adição, visualização e edição apontam para o mesmo componente (`*-form-page.component`).
* **Agrupamento com `children`:** Recursos aninhados (como `/categorias` e `/comorbidades` dentro do módulo `/pessoas`) devem ser organizados usando arrays de rotas filhas (`children: [...]`).

### Configuração de Rotas da Feature (`src/app/features/pessoas/pessoas.routes.ts`)

```typescript
import { Routes } from '@angular/router';

export const PESSOAS_ROUTES: Routes = [
  // ==========================================
  // Recurso: Pessoas
  // ==========================================
  {
    path: '',
    loadComponent: () =>
      import('./pages/pessoa-lista-page/pessoa-lista-page.component').then(
        (m) => m.PessoaListaPageComponent
      ),
  },
  {
    path: 'adicionar',
    loadComponent: () =>
      import('./pages/pessoa-form-page/pessoa-form-page.component').then(
        (m) => m.PessoaFormPageComponent
      ),
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./pages/pessoa-form-page/pessoa-form-page.component').then(
        (m) => m.PessoaFormPageComponent
      ),
    data: { visualizacao: true },
  },
  {
    path: ':id/editar',
    loadComponent: () =>
      import('./pages/pessoa-form-page/pessoa-form-page.component').then(
        (m) => m.PessoaFormPageComponent
      ),
  },

  // ==========================================
  // Recurso: Categorias (Sub-recurso)
  // ==========================================
  {
    path: 'categorias',
    children: [
      {
        path: 'adicionar',
        loadComponent: () =>
          import('./pages/categoria-form-page/categoria-form-page.component').then(
            (m) => m.CategoriaFormPageComponent
          ),
      },
      {
        path: ':id',
        loadComponent: () =>
          import('./pages/categoria-form-page/categoria-form-page.component').then(
            (m) => m.CategoriaFormPageComponent
          ),
        data: { visualizacao: true },
      },
      {
        path: ':id/editar',
        loadComponent: () =>
          import('./pages/categoria-form-page/categoria-form-page.component').then(
            (m) => m.CategoriaFormPageComponent
          ),
      },
    ],
  },

  // ==========================================
  // Recurso: Comorbidades (Sub-recurso)
  // ==========================================
  {
    path: 'comorbidades',
    children: [
      {
        path: 'adicionar',
        loadComponent: () =>
          import('./pages/comorbidade-form-page/comorbidade-form-page.component').then(
            (m) => m.ComorbidadeFormPageComponent
          ),
      },
      {
        path: ':id',
        loadComponent: () =>
          import('./pages/comorbidade-form-page/comorbidade-form-page.component').then(
            (m) => m.ComorbidadeFormPageComponent
          ),
        data: { visualizacao: true },
      },
      {
        path: ':id/editar',
        loadComponent: () =>
          import('./pages/comorbidade-form-page/comorbidade-form-page.component').then(
            (m) => m.ComorbidadeFormPageComponent
          ),
      },
    ],
  },
];

```

# Camadas da Aplicação

## Models / Interfaces

Mantenha nomes e propriedades rigorosamente compatíveis com os Schemas do Swagger.

### Paginação e Parâmetros Genéricos (`src/app/shared/models/`)

#### Paginação de Resposta (`paginacao.model.ts`)

```typescript
export interface RespostaPaginada<T> {
  dados: T[];
  paginaAtual: number;
  tamanhoPagina: number;
  totalElementos: number;
  totalPaginas: number;
}

```

#### Base de Paginação Padrão Spring Data (`parametros-paginacao.model.ts`)

```typescript
export interface ParametrosPaginacao {
  page: number;
  size: number;
  sort?: string; // Formato esperado pelo Spring: "campo,asc" ou "campo,desc"
  incluirInativos?: boolean;
}

```

#### Modelo Genérico para Enums de Domínio (`item-dominio.model.ts`)

```typescript
export interface ItemDominio<T = string> {
  codigo: T;
  descricao: string;
}

```

### Models da Feature (`src/app/features/pessoas/models/pessoa.model.ts`)

```typescript
import { ParametrosPaginacao } from '../../../shared/models/parametros-paginacao.model';
import { ItemDominio } from '../../../shared/models/item-dominio.model';
import { CategoriaResumo } from './categoria.model';
import { ComorbidadeResumo } from './comorbidade.model';

// Enums com Estrutura Dupla (GET: ItemDominio / POST: string)
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

export enum RendaFamiliarCodigo {
  ATE_UM_SALARIO = 'ATE_UM_SALARIO',
  DE_UM_A_DOIS_SALARIOS = 'DE_UM_A_DOIS_SALARIOS',
  DE_DOIS_A_TRES_SALARIOS = 'DE_DOIS_A_TRES_SALARIOS',
  MAIS_DE_TRES_SALARIOS = 'MAIS_DE_TRES_SALARIOS'
}
export type RendaFamiliarItem = ItemDominio<RendaFamiliarCodigo>;

// DTO de Listagem Resumida (Tabela)
export interface PessoaResumo {
  id: number;
  nome: string;
  cpf: string;
  telefone: string;
  email?: string;
  ativo: boolean;
}

// DTO de Detalhamento Completo (Visualização e Edição com dados aninhados)
export interface PessoaDetalhe {
  id: number;
  nome: string;
  cpf: string;
  dataNascimento: string;
  sexo?: SexoItem;
  telefone: string;
  email?: string;
  escolaridade?: EscolaridadeItem;
  profissao?: string;
  rendaFamiliar?: RendaFamiliarItem;
  ativo: boolean;
  comorbidades: ComorbidadeResumo[];
  categorias: CategoriaResumo[];
  descricao?: string;
}

// DTO de Requisição (Envio de códigos primitivos e IDs numéricos)
export interface PessoaRequisicao {
  nome: string;
  cpf: string;
  dataNascimento: string;
  sexo?: SexoCodigo;
  telefone: string;
  email?: string;
  escolaridade?: EscolaridadeCodigo;
  profissao?: string;
  rendaFamiliar?: RendaFamiliarCodigo;
  comorbidades?: number[];
  categorias?: number[];
  descricao?: string;
}

// Filtros específicos por campos para a consulta de Pessoas
export interface PessoaFiltros {
  nome?: string;
  cpf?: string;
  categoriaId?: number;
}

// Parâmetros finais enviados à API
export interface PessoaConsultaParametros extends ParametrosPaginacao, PessoaFiltros {}

```

## Services

Responsáveis pela comunicação HTTP com a API REST, mapeando parâmetros de paginação e filtros específicos via `HttpParams`.

```typescript
import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { RespostaPaginada } from '../../../shared/models/paginacao.model';
import { PessoaConsultaParametros, PessoaDetalhe, PessoaRequisicao, PessoaResumo } from '../models/pessoa.model';

@Injectable({
  providedIn: 'root',
})
export class PessoaService {
  private readonly http = inject(HttpClient);
  private readonly endpoint = 'pessoas';

  listar(parametros?: PessoaConsultaParametros): Observable<RespostaPaginada<PessoaResumo>> {
    let params = new HttpParams();

    if (parametros) {
      if (parametros.page !== undefined) params = params.set('page', parametros.page.toString());
      if (parametros.size !== undefined) params = params.set('size', parametros.size.toString());
      if (parametros.sort) params = params.set('sort', parametros.sort);
      if (parametros.incluirInativos !== undefined) {
        params = params.set('incluirInativos', parametros.incluirInativos.toString());
      }

      // Filtros específicos por campos
      if (parametros.nome) params = params.set('nome', parametros.nome.trim());
      if (parametros.cpf) params = params.set('cpf', parametros.cpf.trim());
      if (parametros.categoriaId) params = params.set('categoriaId', parametros.categoriaId.toString());
    }

    return this.http.get<RespostaPaginada<PessoaResumo>>(this.endpoint, { params });
  }

  buscarPorId(id: number): Observable<PessoaDetalhe> {
    return this.http.get<PessoaDetalhe>(`${this.endpoint}/${id}`);
  }

  cadastrar(requisicao: PessoaRequisicao): Observable<PessoaDetalhe> {
    return this.http.post<PessoaDetalhe>(this.endpoint, requisicao);
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

  excluir(id: number): Observable<void> {
    return this.http.delete<void>(`${this.endpoint}/${id}`);
  }
}

```

### Services Auxiliares de Apoio (Listagem Não Paginada)

Para carregar dropdowns e componentes de seleção (ex.: Categorias e Comorbidades no formulário), os serviços correspondentes devem fornecer o método `listarTodas()` consumindo o endpoint `/todas`:

```typescript
// Exemplo em CategoriaService
listarTodas(incluirInativos: boolean = false): Observable<CategoriaResumo[]> {
  const params = new HttpParams().set('incluirInativos', incluirInativos.toString());
  return this.http.get<CategoriaResumo[]>(`${this.endpoint}/todas`, { params });
}

```

---

# Padrão de Listagens, Tabelas e Filtros

A listagem adota a divisão **Smart/Dumb** orientada a **URL como Fonte da Verdade**:

* **URL State (`QueryParams`):** Paginação, ordenação e filtros ficam sincronizados nos parâmetros da rota ativa.
* **Sanitização de Filtros Vazios:** Filtros em branco ou limpos devem ser transmitidos como `null` ao `router.navigate` para que o Angular remova a chave da URL (evitando queries poluídas como `?nome=&cpf=`).
* **Smart Component (`Page`):** Escuta `route.queryParams`, alimenta a pipeline reativa (`switchMap`) via `toSignal()`, abre o diálogo de filtros e orquestra confirmações de ação.
* **Dumb Components (`Table` e `FiltroDialog`):** Não conhecem Services de API e comunicam intenções por `output()`.
* **Retorno de Formulário:** O componente de formulário utiliza `Location.back()` para restaurar a listagem na exata página, ordenação e filtros previamente aplicados.

---

### 1. Componente Dumb: Tabela (`pessoa-tabela.component.ts`)

```typescript
import { Component, input, output } from '@angular/core';
import { PageEvent, MatPaginatorModule } from '@angular/material/paginator';
import { Sort, MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { PessoaResumo } from '../../models/pessoa.model';

@Component({
  selector: 'app-pessoa-tabela',
  standalone: true,
  imports: [
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule
  ],
  templateUrl: './pessoa-tabela.component.html'
})
export class PessoaTabelaComponent {
  readonly dados = input.required<PessoaResumo[]>();
  readonly totalElementos = input.required<number>();
  readonly tamanhoPagina = input.required<number>();
  readonly paginaAtual = input.required<number>();
  readonly carregando = input.required<boolean>();

  readonly aoMudarPagina = output<PageEvent>();
  readonly aoMudarOrdem = output<Sort>();
  readonly aoVisualizar = output<number>();
  readonly aoEditar = output<number>();
  readonly aoAlternarEstado = output<{ id: number; ativo: boolean }>();
  readonly aoExcluir = output<number>();

  protected readonly colunasExibidas: string[] = ['nome', 'cpf', 'telefone', 'email', 'status', 'acoes'];
}

```

#### Template da Tabela (`pessoa-tabela.component.html`)

```html
<div class="bg-white rounded-lg shadow-sm overflow-hidden border border-gray-100">
  <table mat-table [dataSource]="dados()" matSort (matSortChange)="aoMudarOrdem.emit($event)" class="w-full">

    <ng-container matColumnDef="nome">
      <th mat-header-cell *matHeaderCellDef mat-sort-header="nome">Nome</th>
      <td mat-cell *matCellDef="let p">{{ p.nome }}</td>
    </ng-container>

    <ng-container matColumnDef="cpf">
      <th mat-header-cell *matHeaderCellDef mat-sort-header="cpf">CPF</th>
      <td mat-cell *matCellDef="let p">{{ p.cpf }}</td>
    </ng-container>

    <ng-container matColumnDef="telefone">
      <th mat-header-cell *matHeaderCellDef>Telefone</th>
      <td mat-cell *matCellDef="let p">{{ p.telefone }}</td>
    </ng-container>

    <ng-container matColumnDef="email">
      <th mat-header-cell *matHeaderCellDef>E-mail</th>
      <td mat-cell *matCellDef="let p">{{ p.email || '-' }}</td>
    </ng-container>

    <ng-container matColumnDef="status">
      <th mat-header-cell *matHeaderCellDef>Status</th>
      <td mat-cell *matCellDef="let p">
        <span [class]="p.ativo ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'"
              class="px-2 py-1 rounded-full text-xs font-semibold">
          {{ p.ativo ? 'Ativo' : 'Inativo' }}
        </span>
      </td>
    </ng-container>

    <ng-container matColumnDef="acoes">
      <th mat-header-cell *matHeaderCellDef class="text-right">Ações</th>
      <td mat-cell *matCellDef="let p" class="text-right space-x-1">
        <button mat-icon-button color="primary" matTooltip="Visualizar" (click)="aoVisualizar.emit(p.id)">
          <mat-icon>visibility</mat-icon>
        </button>

        <button mat-icon-button color="primary" matTooltip="Editar" (click)="aoEditar.emit(p.id)">
          <mat-icon>edit</mat-icon>
        </button>

        @if (p.ativo) {
          <button mat-icon-button color="warn" matTooltip="Inativar"
                  (click)="aoAlternarEstado.emit({ id: p.id, ativo: false })">
            <mat-icon>block</mat-icon>
          </button>
        } @else {
          <button mat-icon-button class="text-green-600" matTooltip="Reativar"
                  (click)="aoAlternarEstado.emit({ id: p.id, ativo: true })">
            <mat-icon>check_circle</mat-icon>
          </button>
        }

        <button mat-icon-button color="warn" matTooltip="Excluir Definitivamente" (click)="aoExcluir.emit(p.id)">
          <mat-icon>delete_forever</mat-icon>
        </button>
      </td>
    </ng-container>

    <tr mat-header-row *matHeaderRowDef="colunasExibidas"></tr>
    <tr mat-row *matRowDef="let row; columns: colunasExibidas;"></tr>

    <tr class="mat-row" *matNoDataRow>
      <td class="mat-cell p-4 text-center text-gray-500" [attr.colspan]="colunasExibidas.length">
        @if (carregando()) {
          Carregando registros...
        } @else {
          Nenhum associado encontrado.
        }
      </td>
    </tr>
  </table>

  <mat-paginator
    [length]="totalElementos()"
    [pageSize]="tamanhoPagina()"
    [pageIndex]="paginaAtual()"
    [pageSizeOptions]="[5, 10, 25, 50]"
    (page)="aoMudarPagina.emit($event)"
    aria-label="Selecione a página">
  </mat-paginator>
</div>

```

---

### 2. Componente Dumb: Modal de Filtro por Campos (`pessoa-filtro-dialog.component.ts`)

```typescript
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatButtonModule } from '@angular/material/button';
import { PessoaFiltros } from '../../models/pessoa.model';

export interface PessoaDialogFiltroData extends PessoaFiltros {
  incluirInativos?: boolean;
}

@Component({
  selector: 'app-pessoa-filtro-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatCheckboxModule,
    MatButtonModule
  ],
  template: `
    <h2 mat-dialog-title class="text-lg font-semibold">Filtrar Associados</h2>
    <mat-dialog-content [formGroup]="form" class="flex flex-col gap-3 pt-2">
      <mat-form-field appearance="outline" class="w-full">
        <mat-label>Nome</mat-label>
        <input matInput formControlName="nome" placeholder="Filtrar por nome" />
      </mat-form-field>

      <mat-form-field appearance="outline" class="w-full">
        <mat-label>CPF</mat-label>
        <input matInput formControlName="cpf" placeholder="Filtrar por CPF" />
      </mat-form-field>

      <mat-checkbox formControlName="incluirInativos" color="primary">
        Incluir associados inativos na busca
      </mat-checkbox>
    </mat-dialog-content>

    <mat-dialog-actions align="end" class="gap-2">
      <button mat-button (click)="limpar()">Limpar</button>
      <button mat-button mat-dialog-close>Cancelar</button>
      <button mat-raised-button color="primary" (click)="aplicar()">Aplicar Filtros</button>
    </mat-dialog-actions>
  `
})
export class PessoaFiltroDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<PessoaFiltroDialogComponent>);
  readonly data = inject<PessoaDialogFiltroData>(MAT_DIALOG_DATA, { optional: true });

  protected readonly form = this.fb.group({
    nome: [this.data?.nome || ''],
    cpf: [this.data?.cpf || ''],
    incluirInativos: [this.data?.incluirInativos ?? false]
  });

  aplicar(): void {
    this.dialogRef.close(this.form.value);
  }

  limpar(): void {
    const limpo = { nome: '', cpf: '', incluirInativos: false };
    this.form.reset(limpo);
    this.dialogRef.close(limpo);
  }
}

```

---

### 3. Componente Smart: Página de Listagem (`pessoa-lista-page.component.ts`)

```typescript
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { PageEvent } from '@angular/material/paginator';
import { Sort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { catchError, finalize, map, switchMap, tap } from 'rxjs/operators';
import { of } from 'rxjs';

import { PessoaService } from '../../services/pessoa.service';
import { PessoaConsultaParametros } from '../../models/pessoa.model';
import { DialogoConfirmacaoService } from '../../../shared/services/dialogo-confirmacao.service';
import { NotificacaoService } from '../../../shared/services/notificacao.service';
import { PessoaTabelaComponent } from '../../components/pessoa-tabela/pessoa-tabela.component';
import {
  PessoaFiltroDialogComponent,
  PessoaDialogFiltroData
} from '../../components/pessoa-filtro-dialog/pessoa-filtro-dialog.component';

@Component({
  selector: 'app-pessoa-lista-page',
  standalone: true,
  imports: [
    PessoaTabelaComponent,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './pessoa-lista-page.component.html'
})
export class PessoaListaPageComponent {
  private readonly pessoaService = inject(PessoaService);
  private readonly confirmacao = inject(DialogoConfirmacaoService);
  private readonly notificacao = inject(NotificacaoService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly dialog = inject(MatDialog);

  protected readonly carregando = signal<boolean>(false);

  // 1. Converte a URL ativa na tipagem estrita de parâmetros de busca
  private readonly parametros$ = this.route.queryParams.pipe(
    map((params) => ({
      page: params['page'] ? Number(params['page']) : 0,
      size: params['size'] ? Number(params['size']) : 10,
      sort: params['sort'] || 'nome,asc',
      incluirInativos: params['incluirInativos'] === 'true',
      nome: params['nome'] || undefined,
      cpf: params['cpf'] || undefined,
    } as PessoaConsultaParametros))
  );

  protected readonly parametrosAtuais = toSignal(this.parametros$, {
    initialValue: { page: 0, size: 10, sort: 'nome,asc', incluirInativos: false }
  });

  // 2. Busca reativa declarativa com base na alteração da URL
  private readonly respostaPessoas$ = this.parametros$.pipe(
    tap(() => this.carregando.set(true)),
    switchMap((params) =>
      this.pessoaService.listar(params).pipe(
        finalize(() => this.carregando.set(false)),
        catchError(() => of({ dados: [], paginaAtual: 0, tamanhoPagina: 10, totalElementos: 0, totalPaginas: 0 }))
      )
    )
  );

  protected readonly respostaPessoas = toSignal(this.respostaPessoas$, {
    initialValue: { dados: [], paginaAtual: 0, tamanhoPagina: 10, totalElementos: 0, totalPaginas: 0 }
  });

  // 3. Ações de Navegação
  irParaAdicionar(): void {
    this.router.navigate(['/pessoas/adicionar']);
  }

  visualizarPessoa(id: number): void {
    this.router.navigate(['/pessoas', id]);
  }

  editarPessoa(id: number): void {
    this.router.navigate(['/pessoas', id, 'editar']);
  }

  mudarPagina(event: PageEvent): void {
    this.atualizarUrl({ page: event.pageIndex, size: event.pageSize });
  }

  mudarOrdem(event: Sort): void {
    const direcao = event.direction || 'asc';
    const sort = event.active ? `${event.active},${direcao}` : 'nome,asc';
    this.atualizarUrl({ page: 0, sort });
  }

  abrirFiltros(): void {
    const params = this.parametrosAtuais();

    const dialogRef = this.dialog.open(PessoaFiltroDialogComponent, {
      width: '420px',
      data: {
        nome: params.nome,
        cpf: params.cpf,
        incluirInativos: params.incluirInativos
      } as PessoaDialogFiltroData
    });

    dialogRef.afterClosed().subscribe((filtros: PessoaDialogFiltroData | undefined) => {
      if (filtros !== undefined) {
        // Sanitiza campos vazios enviando null para remover o query param da URL
        this.atualizarUrl({
          page: 0,
          nome: filtros.nome ? filtros.nome.trim() : null,
          cpf: filtros.cpf ? filtros.cpf.trim() : null,
          incluirInativos: filtros.incluirInativos ? 'true' : null
        });
      }
    });
  }

  async alternarEstado(evento: { id: number; ativo: boolean }): Promise<void> {
    const acao = evento.ativo ? 'reativar' : 'inativar';
    const confirmou = await this.confirmacao.confirmar(
      `${evento.ativo ? 'Reativar' : 'Inativar'} Associado`,
      `Tem certeza que deseja ${acao} este registro?`
    );

    if (!confirmou) return;

    const requisicao$ = evento.ativo
      ? this.pessoaService.reativar(evento.id)
      : this.pessoaService.desativar(evento.id);

    requisicao$.subscribe({
      next: () => {
        this.notificacao.sucesso(`Registro ${evento.ativo ? 'reativado' : 'inativado'} com sucesso!`);
        this.atualizarUrl({ _refresh: Date.now() });
      }
    });
  }

  async excluirPessoa(id: number): Promise<void> {
    const confirmou = await this.confirmacao.confirmar(
      'Excluir Associado',
      'ATENÇÃO: Esta ação removerá definitivamente o associado da base. Deseja continuar?'
    );

    if (!confirmou) return;

    this.pessoaService.excluir(id).subscribe({
      next: () => {
        this.notificacao.sucesso('Registro excluído com sucesso!');
        this.atualizarUrl({ _refresh: Date.now() });
      }
    });
  }

  private atualizarUrl(novosParams: Record<string, any>): void {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: novosParams,
      queryParamsHandling: 'merge'
    });
  }
}

```

#### Template da Página (`pessoa-lista-page.component.html`)

```html
<section class="max-w-7xl mx-auto p-6 space-y-6">
  <header class="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
    <div>
      <h1 class="text-2xl font-bold text-gray-800">Associados</h1>
      <p class="text-sm text-gray-500">Gerencie os associados cadastrados na organização</p>
    </div>

    <div class="flex items-center gap-3">
      <button mat-stroked-button color="primary" (click)="abrirFiltros()">
        <mat-icon>filter_list</mat-icon>
        Filtros
      </button>

      <button mat-raised-button color="primary" (click)="irParaAdicionar()">
        <mat-icon>add</mat-icon>
        Adicionar Pessoa
      </button>
    </div>
  </header>

  <app-pessoa-tabela
    [dados]="respostaPessoas().dados"
    [totalElementos]="respostaPessoas().totalElementos"
    [tamanhoPagina]="respostaPessoas().tamanhoPagina"
    [paginaAtual]="respostaPessoas().paginaAtual"
    [carregando]="carregando()"
    (aoMudarPagina)="mudarPagina($event)"
    (aoMudarOrdem)="mudarOrdem($event)"
    (aoVisualizar)="visualizarPessoa($event)"
    (aoEditar)="editarPessoa($event)"
    (aoAlternarEstado)="alternarEstado($event)"
    (aoExcluir)="excluirPessoa($event)">
  </app-pessoa-tabela>
</section>

```

---

# Formulários Unificados (Adição, Visualização e Edição)

Para evitar duplicidade, as operações de **Criação (`/adicionar`)**, **Visualização (`/:id`)** e **Edição (`/:id/editar`)** compartilham o mesmo componente (`*-form-page.component`), herdando toda a estilização do Figma a partir das diretrizes globais sem necessidade de bloco `styles`.

### Diretrizes de Implementação

* **Determinação do Modo de Operação:**
* **Visualização:** Identificada pelo input `visualizacao = input<boolean>(false)`, preenchido via `data: { visualizacao: true }` configurado na rota `:id`. Nesse modo, o método `form.disable()` é acionado para bloquear todas as interações.
* **Edição:** Identificada quando há `id` presente e `visualizacao` for falso.
* **Criação:** Identificada quando não há `id`.


* **Tratamento de Enums no Formulário:**
* No template (`<mat-select>`), faça o *bind* direto com o código primitivo do Enum (`opcao.codigo`):
```html
<mat-select formControlName="sexo">
  @for (opcao of opcoesSexo; track opcao.codigo) {
    <mat-option [value]="opcao.codigo">{{ opcao.descricao }}</mat-option>
  }
</mat-select>

```


* Na carga dos dados (`patchValue`), extraia a string pura do código caso o objeto venha no formato `{ codigo, descricao }`: `sexo: pessoa.sexo?.codigo`.


* **Máscaras e Limpeza de Formatação:** Para campos com formatação visual (CPF, Telefone, Data), aplique máscaras via eventos de input no componente e envie apenas dígitos numéricos brutos à API (`somenteDigitos(valor)`). Datas devem ser convertidas para o padrão aceito pelo backend (`dd-MM-yyyy`).
* **Navegação com Preservação de Estado:** Ao salvar ou cancelar/voltar, utilize `Location.back()`. Isso retorna para o histórico original da listagem com seus queryParams preservados (`page`, `sort`, filtros).
* **Reset Seguro de Formulário:** Após salvar com sucesso, utilize `formDirective.resetForm()` em vez de `form.reset()`, garantindo que os validadores e o estado visual de submissão do Angular Material sejam resetados.
* **Tratamento de Validações da API:** Mapeie erros HTTP 400 (`erros`) diretamente para os controles via `control.setErrors({ backend: e.mensagem })`.

### Componente de Formulário Unificado (`pessoa-form-page.component.ts`)

```typescript
import { Component, effect, inject, input, signal, viewChild } from '@angular/core';
import { Location } from '@angular/common';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import {
  AbstractControl,
  FormGroupDirective,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';

import { PessoaService } from '../../services/pessoa.service';
import { CategoriaService } from '../../services/categoria.service';
import { ComorbidadeService } from '../../services/comorbidade.service';
import { CategoriaResumo } from '../../models/categoria.model';
import { ComorbidadeResumo } from '../../models/comorbidade.model';
import {
  PessoaDetalhe,
  PessoaRequisicao,
  SexoCodigo,
  SexoItem,
  EscolaridadeCodigo,
  EscolaridadeItem,
  RendaFamiliarCodigo,
  RendaFamiliarItem
} from '../../models/pessoa.model';
import { NotificacaoService } from '../../../shared/services/notificacao.service';
import { ErroApi } from '../../../shared/models/erro-api.model';

@Component({
  selector: 'app-pessoa-form-page',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule
  ],
  templateUrl: './pessoa-form-page.component.html'
})
export class PessoaFormPageComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly location = inject(Location);
  private readonly router = inject(Router);
  private readonly notificacaoService = inject(NotificacaoService);
  private readonly pessoaService = inject(PessoaService);
  private readonly categoriaService = inject(CategoriaService);
  private readonly comorbidadeService = inject(ComorbidadeService);
  private readonly formDirective = viewChild(FormGroupDirective);

  // Inputs via Rota
  readonly id = input<string>();
  readonly visualizacao = input<boolean>(false);

  readonly carregando = signal<boolean>(false);
  readonly categorias = signal<CategoriaResumo[]>([]);
  readonly comorbidades = signal<ComorbidadeResumo[]>([]);

  protected readonly opcoesSexo: SexoItem[] = [
    { codigo: SexoCodigo.FEMININO, descricao: 'Feminino' },
    { codigo: SexoCodigo.MASCULINO, descricao: 'Masculino' },
    { codigo: SexoCodigo.PREFIRO_NAO_INFORMAR, descricao: 'Prefiro não informar' }
  ];

  protected readonly opcoesEscolaridade: EscolaridadeItem[] = [
    { codigo: EscolaridadeCodigo.FUNDAMENTAL_INCOMPLETO, descricao: 'Ensino Fundamental Incompleto' },
    { codigo: EscolaridadeCodigo.FUNDAMENTAL_COMPLETO, descricao: 'Ensino Fundamental Completo' },
    { codigo: EscolaridadeCodigo.ENSINO_MEDIO_INCOMPLETO, descricao: 'Ensino Médio Incompleto' },
    { codigo: EscolaridadeCodigo.ENSINO_MEDIO_COMPLETO, descricao: 'Ensino Médio Completo' },
    { codigo: EscolaridadeCodigo.SUPERIOR_INCOMPLETO, descricao: 'Ensino Superior Incompleto' },
    { codigo: EscolaridadeCodigo.SUPERIOR_COMPLETO, descricao: 'Ensino Superior Completo' }
  ];

  protected readonly opcoesRenda: RendaFamiliarItem[] = [
    { codigo: RendaFamiliarCodigo.ATE_UM_SALARIO, descricao: 'Até 1 salário mínimo' },
    { codigo: RendaFamiliarCodigo.DE_UM_A_DOIS_SALARIOS, descricao: 'De 1 a 2 salários mínimos' },
    { codigo: RendaFamiliarCodigo.DE_DOIS_A_TRES_SALARIOS, descricao: 'De 2 a 3 salários mínimos' },
    { codigo: RendaFamiliarCodigo.MAIS_DE_TRES_SALARIOS, descricao: 'Mais de 3 salários mínimos' }
  ];

  protected readonly form = this.fb.group({
    nome: ['', [Validators.required, Validators.pattern(/\S/)]],
    cpf: ['', [Validators.required, cpfValido]],
    dataNascimento: ['', [Validators.required, dataNascimentoValida]],
    telefone: ['', [Validators.required, telefoneValido]],
    email: ['', [Validators.email]],
    sexo: [null as SexoCodigo | null],
    escolaridade: [null as EscolaridadeCodigo | null],
    profissao: [''],
    rendaFamiliar: [null as RendaFamiliarCodigo | null],
    categorias: [[] as number[]],
    comorbidades: [[] as number[]],
    descricao: ['']
  });

  get modoVisualizacao(): boolean {
    return this.visualizacao();
  }

  get modoEdicao(): boolean {
    return !this.modoVisualizacao && !!this.id();
  }

  constructor() {
    this.carregarDadosAuxiliares();

    effect(() => {
      const idRegistro = this.id();
      if (idRegistro) {
        this.carregarDados(Number(idRegistro));
      }
    });
  }

  salvar(): void {
    if (this.form.invalid || this.modoVisualizacao) {
      this.form.markAllAsTouched();
      this.notificacaoService.alerta('Verifique os campos destacados antes de salvar.');
      return;
    }

    this.carregando.set(true);
    const payload = this.montarPayload();

    const requisicao$ = this.modoEdicao
      ? this.pessoaService.atualizar(Number(this.id()), payload)
      : this.pessoaService.cadastrar(payload);

    requisicao$.subscribe({
      next: () => {
        this.notificacaoService.sucesso(`Pessoa ${this.modoEdicao ? 'atualizada' : 'cadastrada'} com sucesso!`);
        this.formDirective()?.resetForm();
        this.voltar();
      },
      error: (err: HttpErrorResponse) => this.tratarErroSubmissao(err),
      complete: () => this.carregando.set(false)
    });
  }

  irParaEdicao(): void {
    this.router.navigate(['/pessoas', this.id(), 'editar']);
  }

  voltar(): void {
    this.location.back();
  }

  protected aplicarMascaraCpf(): void {
    const digitos = somenteDigitos(this.form.controls.cpf.value).slice(0, 11);
    let formatado = digitos;

    if (digitos.length > 9) {
      formatado = `${digitos.slice(0, 3)}.${digitos.slice(3, 6)}.${digitos.slice(6, 9)}-${digitos.slice(9)}`;
    } else if (digitos.length > 6) {
      formatado = `${digitos.slice(0, 3)}.${digitos.slice(3, 6)}.${digitos.slice(6)}`;
    } else if (digitos.length > 3) {
      formatado = `${digitos.slice(0, 3)}.${digitos.slice(3)}`;
    }

    this.form.controls.cpf.setValue(formatado, { emitEvent: false });
  }

  protected aplicarMascaraTelefone(): void {
    const digitos = somenteDigitos(this.form.controls.telefone.value).slice(0, 11);
    let formatado = digitos;

    if (digitos.length > 6) {
      const corte = digitos.length > 10 ? 7 : 6;
      formatado = `(${digitos.slice(0, 2)}) ${digitos.slice(2, corte)}-${digitos.slice(corte)}`;
    } else if (digitos.length > 2) {
      formatado = `(${digitos.slice(0, 2)}) ${digitos.slice(2)}`;
    } else if (digitos.length > 0) {
      formatado = `(${digitos}`;
    }

    this.form.controls.telefone.setValue(formatado, { emitEvent: false });
  }

  protected aplicarMascaraData(): void {
    const digitos = somenteDigitos(this.form.controls.dataNascimento.value).slice(0, 8);
    let formatado = digitos;

    if (digitos.length > 4) {
      formatado = `${digitos.slice(0, 2)}/${digitos.slice(2, 4)}/${digitos.slice(4)}`;
    } else if (digitos.length > 2) {
      formatado = `${digitos.slice(0, 2)}/${digitos.slice(2)}`;
    }

    this.form.controls.dataNascimento.setValue(formatado, { emitEvent: false });
  }

  private carregarDadosAuxiliares(): void {
    this.categoriaService.listarTodas().subscribe({
      next: (lista) => this.categorias.set(lista)
    });

    this.comorbidadeService.listarTodas().subscribe({
      next: (lista) => this.comorbidades.set(lista)
    });
  }

  private carregarDados(id: number): void {
    this.carregando.set(true);
    this.pessoaService.buscarPorId(id).subscribe({
      next: (pessoa) => {
        this.preencherFormulario(pessoa);
        if (this.modoVisualizacao) {
          this.form.disable();
        }
      },
      error: () => this.voltar(),
      complete: () => this.carregando.set(false)
    });
  }

  private preencherFormulario(pessoa: PessoaDetalhe): void {
    const dataFormatada = pessoa.dataNascimento
      ? pessoa.dataNascimento.replace(/-/g, '/')
      : '';

    this.form.patchValue({
      nome: pessoa.nome,
      cpf: pessoa.cpf,
      telefone: pessoa.telefone,
      email: pessoa.email || '',
      dataNascimento: dataFormatada,
      sexo: pessoa.sexo?.codigo || null,
      escolaridade: pessoa.escolaridade?.codigo || null,
      profissao: pessoa.profissao || '',
      rendaFamiliar: pessoa.rendaFamiliar?.codigo || null,
      categorias: pessoa.categorias ? pessoa.categorias.map((c) => c.id) : [],
      comorbidades: pessoa.comorbidades ? pessoa.comorbidades.map((c) => c.id) : [],
      descricao: pessoa.descricao || ''
    });

    this.aplicarMascaraCpf();
    this.aplicarMascaraTelefone();
  }

  private montarPayload(): PessoaRequisicao {
    const raw = this.form.getRawValue();
    return {
      nome: raw.nome.trim(),
      cpf: somenteDigitos(raw.cpf),
      dataNascimento: raw.dataNascimento.replace(/\//g, '-'),
      telefone: somenteDigitos(raw.telefone),
      email: raw.email.trim() || undefined,
      sexo: raw.sexo || undefined,
      escolaridade: raw.escolaridade || undefined,
      profissao: raw.profissao.trim() || undefined,
      rendaFamiliar: raw.rendaFamiliar || undefined,
      categorias: raw.categorias.length > 0 ? raw.categorias : undefined,
      comorbidades: raw.comorbidades.length > 0 ? raw.comorbidades : undefined,
      descricao: raw.descricao.trim() || undefined
    };
  }

  private tratarErroSubmissao(err: HttpErrorResponse): void {
    this.carregando.set(false);
    const corpo = err.error as ErroApi | undefined;

    corpo?.erros?.forEach(({ campo, mensagem }) => {
      this.form.get(campo)?.setErrors({ backend: mensagem });
    });

    if (err.status === 409) {
      this.form.controls.cpf.setErrors({
        backend: corpo?.detail ?? 'Já existe uma pessoa cadastrada com este CPF.'
      });
    }

    this.form.markAllAsTouched();
  }
}

// Funções Utilitárias de Validação
function somenteDigitos(valor: string): string {
  return valor.replace(/\D/g, '');
}

function cpfValido(control: AbstractControl): ValidationErrors | null {
  const digitos = somenteDigitos(String(control.value ?? ''));
  if (!digitos) return null;
  if (digitos.length !== 11) return { cpfIncompleto: true };
  if (/^(\d)\1{10}$/.test(digitos)) return { cpfInvalido: true };

  const digitoVerificador = (quantidade: number): number => {
    let soma = 0;
    for (let posicao = 0; posicao < quantidade; posicao++) {
      soma += Number(digitos[posicao]) * (quantidade + 1 - posicao);
    }
    const resto = (soma * 10) % 11;
    return resto === 10 ? 0 : resto;
  };

  const d1 = digitoVerificador(9) === Number(digitos[9]);
  const d2 = digitoVerificador(10) === Number(digitos[10]);
  return d1 && d2 ? null : { cpfInvalido: true };
}

function dataNascimentoValida(control: AbstractControl): ValidationErrors | null {
  const valor = String(control.value ?? '');
  if (!valor) return null;

  const partes = /^(\d{2})\/(\d{2})\/(\d{4})$/.exec(valor);
  if (!partes) return { dataFormato: true };

  const dia = Number(partes[1]);
  const mes = Number(partes[2]);
  const ano = Number(partes[3]);
  const data = new Date(ano, mes - 1, dia);

  const existe = data.getFullYear() === ano && data.getMonth() === mes - 1 && data.getDate() === dia;
  if (!existe || ano < 1900) return { dataInvalida: true };

  const hoje = new Date();
  hoje.setHours(0, 0, 0, 0);
  return data > hoje ? { dataFutura: true } : null;
}

function telefoneValido(control: AbstractControl): ValidationErrors | null {
  const digitos = somenteDigitos(String(control.value ?? ''));
  if (!digitos) return null;
  return /^\d{10,11}$/.test(digitos) ? null : { telefoneInvalido: true };
}

```

#### Template de Formulário Unificado (`pessoa-form-page.component.html`)

```html
<section class="max-w-4xl mx-auto p-6 bg-white rounded-lg shadow-sm border border-gray-100">
  <h1 class="text-2xl font-bold mb-6">
    @if (modoVisualizacao) {
      Visualizar Pessoa
    } @else if (modoEdicao) {
      Editar Pessoa
    } @else {
      Adicionar Pessoa
    }
  </h1>

  <form [formGroup]="form" (ngSubmit)="salvar()" class="space-y-4">
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <mat-form-field appearance="outline" class="w-full md:col-span-2">
        <mat-label>Nome Completo</mat-label>
        <input matInput formControlName="nome" placeholder="Digite o nome completo" />
        @if (form.get('nome')?.hasError('required')) {
          <mat-error>Nome é obrigatório</mat-error>
        }
        @if (form.get('nome')?.hasError('backend')) {
          <mat-error>{{ form.get('nome')?.getError('backend') }}</mat-error>
        }
      </mat-form-field>

      <mat-form-field appearance="outline" class="w-full">
        <mat-label>CPF</mat-label>
        <input matInput formControlName="cpf" (input)="aplicarMascaraCpf()" placeholder="000.000.000-00" />
        @if (form.get('cpf')?.hasError('required')) {
          <mat-error>CPF é obrigatório</mat-error>
        }
        @if (form.get('cpf')?.hasError('cpfIncompleto')) {
          <mat-error>CPF incompleto</mat-error>
        }
        @if (form.get('cpf')?.hasError('cpfInvalido')) {
          <mat-error>CPF inválido</mat-error>
        }
        @if (form.get('cpf')?.hasError('backend')) {
          <mat-error>{{ form.get('cpf')?.getError('backend') }}</mat-error>
        }
      </mat-form-field>

      <mat-form-field appearance="outline" class="w-full">
        <mat-label>Telefone</mat-label>
        <input matInput formControlName="telefone" (input)="aplicarMascaraTelefone()" placeholder="(00) 00000-0000" />
        @if (form.get('telefone')?.hasError('required')) {
          <mat-error>Telefone é obrigatório</mat-error>
        }
        @if (form.get('telefone')?.hasError('telefoneInvalido')) {
          <mat-error>Telefone inválido</mat-error>
        }
      </mat-form-field>

      <mat-form-field appearance="outline" class="w-full">
        <mat-label>Data de Nascimento</mat-label>
        <input matInput formControlName="dataNascimento" (input)="aplicarMascaraData()" placeholder="dd/mm/aaaa" />
        @if (form.get('dataNascimento')?.hasError('required')) {
          <mat-error>Data é obrigatória</mat-error>
        }
        @if (form.get('dataNascimento')?.hasError('dataFormato')) {
          <mat-error>Formato inválido (dd/mm/aaaa)</mat-error>
        }
        @if (form.get('dataNascimento')?.hasError('dataInvalida')) {
          <mat-error>Data inexistente</mat-error>
        }
        @if (form.get('dataNascimento')?.hasError('dataFutura')) {
          <mat-error>Data não pode ser futura</mat-error>
        }
      </mat-form-field>

      <mat-form-field appearance="outline" class="w-full">
        <mat-label>E-mail</mat-label>
        <input matInput formControlName="email" type="email" placeholder="exemplo@email.com" />
        @if (form.get('email')?.hasError('email')) {
          <mat-error>E-mail inválido</mat-error>
        }
      </mat-form-field>

      <mat-form-field appearance="outline" class="w-full">
        <mat-label>Sexo</mat-label>
        <mat-select formControlName="sexo">
          @for (opcao of opcoesSexo; track opcao.codigo) {
            <mat-option [value]="opcao.codigo">{{ opcao.descricao }}</mat-option>
          }
        </mat-select>
      </mat-form-field>

      <mat-form-field appearance="outline" class="w-full">
        <mat-label>Escolaridade</mat-label>
        <mat-select formControlName="escolaridade">
          @for (opcao of opcoesEscolaridade; track opcao.codigo) {
            <mat-option [value]="opcao.codigo">{{ opcao.descricao }}</mat-option>
          }
        </mat-select>
      </mat-form-field>

      <mat-form-field appearance="outline" class="w-full">
        <mat-label>Profissão</mat-label>
        <input matInput formControlName="profissao" placeholder="Profissão ou ocupação" />
      </mat-form-field>

      <mat-form-field appearance="outline" class="w-full">
        <mat-label>Faixa de Renda Familiar</mat-label>
        <mat-select formControlName="rendaFamiliar">
          @for (opcao of opcoesRenda; track opcao.codigo) {
            <mat-option [value]="opcao.codigo">{{ opcao.descricao }}</mat-option>
          }
        </mat-select>
      </mat-form-field>

      <mat-form-field appearance="outline" class="w-full md:col-span-2">
        <mat-label>Categorias</mat-label>
        <mat-select formControlName="categorias" multiple>
          @for (cat of categorias(); track cat.id) {
            <mat-option [value]="cat.id">{{ cat.nome }}</mat-option>
          }
        </mat-select>
      </mat-form-field>

      <mat-form-field appearance="outline" class="w-full md:col-span-2">
        <mat-label>Comorbidades</mat-label>
        <mat-select formControlName="comorbidades" multiple>
          @for (com of comorbidades(); track com.id) {
            <mat-option [value]="com.id">{{ com.nome }}</mat-option>
          }
        </mat-select>
      </mat-form-field>

      <mat-form-field appearance="outline" class="w-full md:col-span-2">
        <mat-label>Observações / Descrição</mat-label>
        <textarea matInput formControlName="descricao" rows="3" placeholder="Informações complementares"></textarea>
      </mat-form-field>
    </div>

    <!-- Barra de Ações -->
    <div class="flex flex-col-reverse sm:flex-row justify-end gap-3 pt-4 border-t border-gray-100">
      <button mat-flat-button type="button" (click)="voltar()">
        {{ modoVisualizacao ? 'Voltar' : 'Cancelar' }}
      </button>

      @if (modoVisualizacao) {
        <button mat-flat-button color="primary" type="button" (click)="irParaEdicao()">
          Editar Registro
        </button>
      } @else {
        <button
          mat-flat-button
          color="primary"
          type="submit"
          [disabled]="carregando()">
          Salvar
        </button>
      }
    </div>
  </form>
</section>

```

---

# Utilitários Globais (`shared/` e `core/`)

## Serviço de Notificação / Toast (`src/app/shared/services/notificacao.service.ts`)

```typescript
import { inject, Injectable } from '@angular/core';
import { MatSnackBar, MatSnackBarHorizontalPosition, MatSnackBarVerticalPosition } from '@angular/material/snack-bar';

export interface NotificacaoOpcoes {
  duracao?: number;
  posicaoVertical?: MatSnackBarVerticalPosition;
  posicaoHorizontal?: MatSnackBarHorizontalPosition;
}

@Injectable({
  providedIn: 'root',
})
export class NotificacaoService {
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
      horizontalPosition: opcoes?.posicaoHorizontal ?? 'center',
    });
  }
}

```

## Modal Genérico de Confirmação (`src/app/shared/services/dialogo-confirmacao.service.ts`)

```typescript
import { inject, Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { firstValueFrom } from 'rxjs';
import { DialogoConfirmacaoComponent, OptionDialogData } from '../components/dialogo-confirmacao.component';

@Injectable({
  providedIn: 'root',
})
export class DialogoConfirmacaoService {
  private readonly dialog = inject(MatDialog);

  async confirmar(titulo: string, mensagem: string): Promise<boolean> {
    const dialogRef = this.dialog.open<DialogoConfirmacaoComponent, OptionDialogData, boolean>(
      DialogoConfirmacaoComponent,
      {
        data: { titulo, mensagem },
        width: '400px',
      }
    );

    const resultado = await firstValueFrom(dialogRef.afterClosed());
    return !!resultado;
  }
}

```

## Interceptor de URL Base da API (`src/app/core/interceptors/api.interceptor.ts`)

```typescript
import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../../../enviroments/enviroment';

export const apiInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.url.startsWith('http://') || req.url.startsWith('https://')) {
    return next(req);
  }

  const requisicaoComPrefixo = req.clone({
    url: `${environment.apiUrl}/${req.url.replace(/^\//, '')}`,
  });

  return next(requisicaoComPrefixo);
};

```

# Tratamento Global de Erros HTTP (RFC 7807)

```typescript
import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { NotificacaoService } from '../../shared/services/notificacao.service';
import { ErroApi } from '../../shared/models/erro-api.model';

export const erroInterceptor: HttpInterceptorFn = (req, next) => {
  const notificacao = inject(NotificacaoService);
  const router = inject(Router);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 0) {
        notificacao.erro('Servidor indisponível. Verifique sua conexão.');
        return throwError(() => error);
      }

      const corpo = error.error as ErroApi | undefined;
      const detalhe = corpo?.detail || 'Ocorreu um erro ao processar sua solicitação.';

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

      return throwError(() => error);
    })
  );
};

```
