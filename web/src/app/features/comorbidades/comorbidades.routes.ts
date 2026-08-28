import { Routes } from '@angular/router';

export const COMORBIDADES_ROUTES: Routes = [
  {
    path: 'novo',
    loadComponent: () =>
      import('./pages/comorbidade-cadastro-page/comorbidade-cadastro-page.component').then(
        (modulo) => modulo.ComorbidadeCadastroPageComponent,
      ),
  },
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'novo',
  },
];
