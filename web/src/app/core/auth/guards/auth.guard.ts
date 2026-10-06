import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (_route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const notificacao = inject(NotificacaoService);

  if (authService.estaAutenticado()) {
    return true;
  }

  notificacao.alerta('Faça login para continuar.');
  return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
};
