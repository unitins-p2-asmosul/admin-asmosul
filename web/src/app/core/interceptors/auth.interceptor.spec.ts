import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { authInterceptorFn } from './auth.interceptor';
import { AuthService } from '../auth/services/auth.service';

describe('authInterceptorFn', () => {
  let httpClient: HttpClient;
  let httpTesting: HttpTestingController;
  let authServiceMock: {
    obterToken: ReturnType<typeof vi.fn>;
  };

  beforeEach(() => {
    authServiceMock = {
      obterToken: vi.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        provideHttpClient(withInterceptors([authInterceptorFn])),
        provideHttpClientTesting(),
      ],
    });

    httpClient = TestBed.inject(HttpClient);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('deve anexar o header Authorization quando houver token ativo', () => {
    authServiceMock.obterToken.mockReturnValue('meu-jwt-token-123');

    httpClient.get('/api/pessoas').subscribe();

    const req = httpTesting.expectOne('/api/pessoas');
    expect(req.request.headers.has('Authorization')).toBe(true);
    expect(req.request.headers.get('Authorization')).toBe('Bearer meu-jwt-token-123');
    req.flush([]);
  });

  it('não deve anexar o header Authorization quando não houver token ativo', () => {
    authServiceMock.obterToken.mockReturnValue(null);

    httpClient.get('/api/pessoas').subscribe();

    const req = httpTesting.expectOne('/api/pessoas');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush([]);
  });

  it('não deve anexar o token em chamadas para APIs externas como o ViaCEP', () => {
    authServiceMock.obterToken.mockReturnValue('meu-jwt-token-123');

    httpClient.get('https://viacep.com.br/ws/77001000/json').subscribe();

    const req = httpTesting.expectOne('https://viacep.com.br/ws/77001000/json');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });
});
