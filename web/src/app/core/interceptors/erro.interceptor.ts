import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { catchError, throwError } from 'rxjs';
import { ErroApi } from '@features/shared/models/erro-api.model';

export const erroInterceptorFn: HttpInterceptorFn = (req, next) => {
  const notificacao = inject(NotificacaoService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // 1. Falha de rede ou backend indisponível
      if (error.status === 0) {
        notificacao.erro('Servidor indisponível. Verifique sua conexão.');
        return throwError(() => error);
      }

      const corpo = error.error as ErroApi | undefined;
      const detalhe = corpo?.detail || 'Ocorreu um erro ao processar sua solicitação.';

      // 2. Roteamento semântico de notificações
      switch (error.status) {
        case 400:
          if (corpo?.erros && corpo.erros.length > 0) {
            const primeiro = corpo.erros[0];
            notificacao.alerta(`${primeiro.campo}: ${primeiro.mensagem}`);
          } else {
            notificacao.alerta(detalhe);
          }
          break;

        case 403:
          notificacao.erro('Você não tem permissão para realizar esta operação.');
          break;

        case 404:
        case 409:
          notificacao.alerta(detalhe);
          break;

        case 500:
        default:
          notificacao.erro(detalhe);
          break;
      }

      // Propaga o erro caso a página precise reagir (ex: destravar loading)
      return throwError(() => error);
    }),
  );
};
