import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { Perfil } from '../models/auth.model';
import { AuthService } from '../services/auth.service';

/** Libera a rota se o usuário tiver ao menos um dos perfis informados. */
export const perfilGuard =
  (...perfisPermitidos: Perfil[]): CanActivateFn =>
    (_route, state) => {
      const authService = inject(AuthService);
      const router = inject(Router);
      const notificacao = inject(NotificacaoService);

      if (!authService.estaAutenticado()) {
        notificacao.alerta('Faça login para continuar.');
        return router.createUrlTree(['/login'], { queryParams: { returnUrl: state.url } });
      }

      if (perfisPermitidos.some((perfil) => authService.temPerfil(perfil))) {
        return true;
      }

      notificacao.erro('Você não tem permissão para acessar este módulo.');
      return router.createUrlTree(['/']);
    };
