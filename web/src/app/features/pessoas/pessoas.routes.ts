import { Routes } from '@angular/router';

export const PESSOAS_ROUTES: Routes = [
  // ==========================================
  // Recurso: Pessoas
  // ==========================================
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'adicionar', // Alterne para a página de listagem quando ela for criada
  },
  {
    path: 'adicionar',
    loadComponent: () =>
      import('./pages/pessoa-cadastro-page/pessoa-cadastro-page.component').then(
        (m) => m.PessoaCadastroPageComponent
      ),
  },
  {
    path: ':id/editar',
    loadComponent: () =>
      import('./pages/pessoa-cadastro-page/pessoa-cadastro-page.component').then(
        (m) => m.PessoaCadastroPageComponent
      ),
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./pages/pessoa-cadastro-page/pessoa-cadastro-page.component').then(
        (m) => m.PessoaCadastroPageComponent
      ),
    data: {
      visualizacao: true
    }
  },

  // ==========================================
  // Recurso: Comorbidades
  // ==========================================
  {
    path: 'comorbidades',
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'adicionar', // Alterne para a página de listagem de comorbidades
      },
      {
        path: 'adicionar',
        loadComponent: () =>
          import('./pages/comorbidade-cadastro-page/comorbidade-cadastro-page.component').then(
            (m) => m.ComorbidadeCadastroPageComponent
          ),
      },
      {
        path: ':id/editar',
        loadComponent: () =>
          import('./pages/comorbidade-cadastro-page/comorbidade-cadastro-page.component').then(
            (m) => m.ComorbidadeCadastroPageComponent
          ),
      },
      {
        path: ':id',
        loadComponent: () =>
          import('./pages/comorbidade-cadastro-page/comorbidade-cadastro-page.component').then(
            (m) => m.ComorbidadeCadastroPageComponent
          ),
        data: { visualizacao: true },
      },

    ],
  },

  // ==========================================
  // Recurso: Categorias
  // ==========================================
  {
    path: 'categorias',
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'adicionar', // Alterne para a página de listagem de categorias
      },
      {
        path: 'adicionar',
        loadComponent: () =>
          import('./pages/categoria-cadastro-page/categoria-cadastro-page.component').then(
            (m) => m.CategoriaCadastroPageComponent
          ),
      },
      {
        path: ':id/editar',
        loadComponent: () =>
          import('./pages/categoria-cadastro-page/categoria-cadastro-page.component').then(
            (m) => m.CategoriaCadastroPageComponent
          ),
      },
      {
        path: ':id',
        loadComponent: () =>
          import('./pages/categoria-cadastro-page/categoria-cadastro-page.component').then(
            (m) => m.CategoriaCadastroPageComponent
          ),
        data: { visualizacao: true },
      },
    ],
  },
];
