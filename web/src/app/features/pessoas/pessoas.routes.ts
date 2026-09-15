import { Routes } from '@angular/router';

/**
 * Os sub-recursos (comorbidades, categorias) ficam ANTES das rotas de pessoas.
 * A rota `:id` de pessoas casa com qualquer segmento — se viesse primeiro,
 * `/pessoas/comorbidades` seria tratado como uma pessoa de id "comorbidades".
 */
export const PESSOAS_ROUTES: Routes = [
  // ==========================================
  // Recurso: Comorbidades (sub-recurso)
  // ==========================================
  {
    path: 'comorbidades',
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./pages/comorbidade-lista-page/comorbidade-lista-page.component').then(
            (m) => m.ComorbidadeListaPageComponent,
          ),
      },
      {
        path: 'adicionar',
        loadComponent: () =>
          import('./pages/comorbidade-form-page/comorbidade-form-page.component').then(
            (m) => m.ComorbidadeFormPageComponent,
          ),
      },
      {
        path: ':id/editar',
        loadComponent: () =>
          import('./pages/comorbidade-form-page/comorbidade-form-page.component').then(
            (m) => m.ComorbidadeFormPageComponent,
          ),
      },
      {
        path: ':id',
        loadComponent: () =>
          import('./pages/comorbidade-form-page/comorbidade-form-page.component').then(
            (m) => m.ComorbidadeFormPageComponent,
          ),
        data: { visualizacao: true },
      },
    ],
  },

  // ==========================================
  // Recurso: Categorias (sub-recurso)
  // ==========================================
  {
    path: 'categorias',
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./pages/categoria-lista-page/categoria-lista-page.component').then(
            (m) => m.CategoriaListaPageComponent,
          ),
      },
      {
        path: 'adicionar',
        loadComponent: () =>
          import('./pages/categoria-cadastro-page/categoria-cadastro-page.component').then(
            (m) => m.CategoriaCadastroPageComponent,
          ),
      },
      {
        path: ':id/editar',
        loadComponent: () =>
          import('./pages/categoria-cadastro-page/categoria-cadastro-page.component').then(
            (m) => m.CategoriaCadastroPageComponent,
          ),
      },
      {
        path: ':id',
        loadComponent: () =>
          import('./pages/categoria-cadastro-page/categoria-cadastro-page.component').then(
            (m) => m.CategoriaCadastroPageComponent,
          ),
        data: { visualizacao: true },
      },
    ],
  },

  // ==========================================
  // Recurso: Pessoas
  // ==========================================
  {
    path: '',
    loadComponent: () =>
      import('./pages/pessoa-lista-page/pessoa-lista-page.component').then(
        (m) => m.PessoaListaPageComponent,
      ),
  },
  {
    path: 'adicionar',
    loadComponent: () =>
      import('./pages/pessoa-cadastro-page/pessoa-cadastro-page.component').then(
        (m) => m.PessoaCadastroPageComponent,
      ),
  },
  {
    path: ':id/editar',
    loadComponent: () =>
      import('./pages/pessoa-cadastro-page/pessoa-cadastro-page.component').then(
        (m) => m.PessoaCadastroPageComponent,
      ),
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./pages/pessoa-cadastro-page/pessoa-cadastro-page.component').then(
        (m) => m.PessoaCadastroPageComponent,
      ),
    data: {
      visualizacao: true,
    },
  },
];
