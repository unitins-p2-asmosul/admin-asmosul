import { Routes } from '@angular/router';

export const DOACOES_ROUTES: Routes = [
  {
    path: 'categorias',
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./pages/categoria-item-lista-page/categoria-item-lista-page.component').then(
            (modulo) => modulo.CategoriaItemListaPageComponent,
          ),
      },
      {
        path: 'adicionar',
        loadComponent: () =>
          import('./pages/categoria-item-cadastro-page/categoria-item-cadastro-page.component').then(
            (modulo) => modulo.CategoriaItemCadastroPageComponent,
          ),
      },
      {
        path: ':id/editar',
        loadComponent: () =>
          import('./pages/categoria-item-cadastro-page/categoria-item-cadastro-page.component').then(
            (modulo) => modulo.CategoriaItemCadastroPageComponent,
          ),
      },
      {
        path: ':id',
        loadComponent: () =>
          import('./pages/categoria-item-cadastro-page/categoria-item-cadastro-page.component').then(
            (modulo) => modulo.CategoriaItemCadastroPageComponent,
          ),
        data: { visualizacao: true },
      },
    ],
  },
];
