import {
  ApplicationConfig,
  inject,
  LOCALE_ID,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners
} from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';

import { routes } from './app.routes';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { erroInterceptorFn } from '@core/interceptors/erro.interceptor';
import { apiInterceptorFn } from '@core/interceptors/api.interceptor';
import { authInterceptorFn } from '@core/interceptors/auth.interceptor';
import { AuthService } from "@core/auth/services/auth.service";
import {registerLocaleData} from '@angular/common';
import localePt from '@angular/common/locales/pt';


registerLocaleData(localePt);

export const appConfig: ApplicationConfig = {
  providers: [
    provideAppInitializer(() => inject(AuthService).restaurarSessao()),
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withInterceptors([apiInterceptorFn, authInterceptorFn, erroInterceptorFn])),
    { provide: LOCALE_ID, useValue: 'pt-BR' },
  ],
};
