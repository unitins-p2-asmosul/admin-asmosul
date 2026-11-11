import { Routes } from '@angular/router';

export const RELATORIOS_ROUTES: Routes = [
  {
    path: 'pessoas',
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./pages/pessoas-relatorio-page/pessoas-relatorio-page.component').then(
            (modulo) => modulo.PessoasRelatorioPageComponent,
          ),
      },
      {
        path: 'comorbidades',
        loadComponent: () =>
          import('./pages/comorbidades-relatorio-page/comorbidades-relatorio-page.component').then(
            (modulo) => modulo.ComorbidadesRelatorioPageComponent,
          ),
      },
      {
        path: 'categorias',
        loadComponent: () =>
          import('./pages/categoria-pessoas-relatorio-page/categoria-pessoas-relatorio-page.component').then(
            (modulo) => modulo.CategoriaPessoasRelatorioPageComponent,
          ),
      },
    ]
  },
  {
    path: 'doacoes',
    children: [
      {
        path: 'itens',
        loadComponent: () =>
          import('./pages/itens-relatorio-page/itens-relatorio-page.component').then(
            (modulo) => modulo.ItensRelatorioPageComponent,
          ),
      },
      {
        path: 'enderecos',
        loadComponent: () =>
          import('./pages/armazenamentos-relatorio-page/armazenamentos-relatorios-page.component').then(
            (modulo) => modulo.ArmazenamentosRelatorioPageComponent
          )
      },
      {
        path: 'estoque',
        loadComponent: () =>
          import('./pages/estoque-relatorio-page/estoque-relatorio-page.component').then(
            (modulo) => modulo.EstoqueRelatorioPageComponent
          )
      },
      {
        path: 'categorias',
        loadComponent: () =>
          import('./pages/categoria-itens-relatorio-page/categoria-itens-relatorio-page.component').then(
            (modulo) => modulo.CategoriaItensRelatorioPageComponent
          )
      }
    ]
  }
];
