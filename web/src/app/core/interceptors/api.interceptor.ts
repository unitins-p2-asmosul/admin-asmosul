import {HttpInterceptorFn} from '@angular/common/http';
import {environment} from '../../../enviroments/enviroment';

export const apiInterceptorFn: HttpInterceptorFn = (req, next) => {

  // Ignora requisições completas (ex.: chamadas externas para ViaCEP)
  if (req.url.startsWith('http://') || req.url.startsWith('https://')) {
    return next(req);
  }

  const requisicaoComPrefixo = req.clone({
    url: `${environment.apiUrl}/${req.url.replace(/^\//, '')}`
  });

  return next(requisicaoComPrefixo);
};
