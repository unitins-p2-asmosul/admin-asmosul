import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../auth/services/auth.service';

export const authInterceptorFn: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.obterToken();

  if (!token) {
    return next(req);
  }

  // Evita anexar o token do sistema a chamadas externas (ex.: ViaCEP)
  if (req.url.includes('viacep.com.br')) {
    return next(req);
  }

  const requisicaoAutenticada = req.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`,
    },
  });

  return next(requisicaoAutenticada);
};
