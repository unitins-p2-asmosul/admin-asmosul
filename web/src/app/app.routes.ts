import { Routes } from '@angular/router';
import { LoginComponent } from '@features/acessos/pages/login/login.component';
import { authGuard } from './core/auth/guards/auth.guard';
import { perfilGuard } from './core/auth/guards/perfil.guard';
import { Perfil } from "@core/auth/models/auth.model";

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./home.component').then((m) => m.HomeComponent),
  },
  {
    path: 'pessoas',
    canActivate: [perfilGuard(Perfil.GERENCIADOR_PESSOAS)],
    loadChildren: () =>
      import('@features/pessoas/pessoas.routes').then((m) => m.PESSOAS_ROUTES),
  },
  {
    path: 'acessos',
    canActivate: [perfilGuard(Perfil.GERENCIADOR_ACESSO)],
    loadChildren: () =>
      import('@features/acessos/contas.routes').then((m) => m.CONTAS_ROUTES),
  },
];
