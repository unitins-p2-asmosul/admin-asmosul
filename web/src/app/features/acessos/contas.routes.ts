import { Routes } from '@angular/router';

export const CONTAS_ROUTES: Routes = [
  {
    path: 'perfis',
    loadComponent: () =>
      import('./pages/perfil-lista-page/perfil-lista-page.component').then(
        (modulo) => modulo.PerfilListaPageComponent,
      ),
  },
  {
    path: '',
    loadComponent: () =>
      import('./pages/conta-lista-page/conta-lista-page.component').then(
        (modulo) => modulo.ContaListaPageComponent,
      ),
  },
  {
    path: 'adicionar',
    loadComponent: () =>
      import('./pages/conta-cadastro-page/conta-cadastro-page.component').then(
        (modulo) => modulo.ContaCadastroPageComponent,
      ),
  },
  {
    path: ':id/editar',
    loadComponent: () =>
      import('./pages/conta-cadastro-page/conta-cadastro-page.component').then(
        (modulo) => modulo.ContaCadastroPageComponent,
      ),
    data: { visualizacao: false },
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./pages/conta-cadastro-page/conta-cadastro-page.component').then(
        (modulo) => modulo.ContaCadastroPageComponent,
      ),
    data: { visualizacao: true },
  },
];