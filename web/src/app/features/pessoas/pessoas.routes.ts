  import { Routes } from '@angular/router';

export const PESSOAS_ROUTES: Routes = [
  {
    path: 'novo',
    loadComponent: () =>
      import('./pages/pessoa-cadastro-page/pessoa-cadastro-page.component').then(
        (modulo) => modulo.PessoaCadastroPageComponent,
      ),
  },
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'novo',
  },
];
