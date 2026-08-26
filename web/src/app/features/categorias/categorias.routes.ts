import { Routes } from '@angular/router';

export const CATEGORIAS_ROUTES: Routes = [
  {
    path: 'novo',
    loadComponent: () =>
      import('./pages/categoria-cadastro-page/categoria-cadastro-page.component').then(
        (modulo) => modulo.CategoriaCadastroPageComponent,
      ),
  },
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'novo',
  },
];