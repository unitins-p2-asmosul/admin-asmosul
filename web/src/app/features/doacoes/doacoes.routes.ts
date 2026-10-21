import { Routes } from '@angular/router';

export const DOACOES_ROUTES: Routes = [
  {
    path: 'enderecos',
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./pages/endereco-armazenamento-lista-page/endereco-armazenamento-lista-page.component').then(
            (modulo) => modulo.EnderecoArmazenamentoListaPageComponent,
          ),
      },
      {
        path: 'adicionar',
        loadComponent: () =>
          import('./pages/endereco-armazenamento-cadastro-page/endereco-armazenamento-cadastro-page.component').then(
            (modulo) => modulo.EnderecoArmazenamentoCadastroPageComponent,
          ),
      },
      {
        path: ':id/editar',
        loadComponent: () =>
          import('./pages/endereco-armazenamento-cadastro-page/endereco-armazenamento-cadastro-page.component').then(
            (modulo) => modulo.EnderecoArmazenamentoCadastroPageComponent,
          ),
      },
      {
        path: ':id',
        loadComponent: () =>
          import('./pages/endereco-armazenamento-cadastro-page/endereco-armazenamento-cadastro-page.component').then(
            (modulo) => modulo.EnderecoArmazenamentoCadastroPageComponent,
          ),
        data: { visualizacao: true },
      },
    ],
  },
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
  {
    path: 'itens',
    children: [
      {
        path: 'adicionar',
        loadComponent: () =>
          import('./pages/item-cadastro-page/item-cadastro-page.component').then(
            (modulo) => modulo.ItemCadastroPageComponent,
          ),
      },
      {
        path: ':id/editar',
        loadComponent: () =>
          import('./pages/item-cadastro-page/item-cadastro-page.component').then(
            (modulo) => modulo.ItemCadastroPageComponent,
          ),
      },
      {
        path: ':id',
        loadComponent: () =>
          import('./pages/item-cadastro-page/item-cadastro-page.component').then(
            (modulo) => modulo.ItemCadastroPageComponent,
          ),
        data: { visualizacao: true },
      },
    ],
  },
];
