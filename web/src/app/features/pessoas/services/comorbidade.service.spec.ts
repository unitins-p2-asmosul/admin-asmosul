import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { ComorbidadeService } from './comorbidade.service';

describe('ComorbidadeService', () => {
  let service: ComorbidadeService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ComorbidadeService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('envia incluirInativos para a listagem paginada', () => {
    service.listar({ page: 0, size: 20, apenasInativos: true }).subscribe();

    const requisicao = http.expectOne((request) => request.url === 'pessoas/comorbidades');
    expect(requisicao.request.method).toBe('GET');
    expect(requisicao.request.params.get('incluirInativos')).toBe('true');
    expect(requisicao.request.params.has('apenasInativos')).toBe(false);
    requisicao.flush({
      dados: [],
      paginaAtual: 0,
      tamanhoPagina: 20,
      totalElementos: 0,
      totalPaginas: 0,
    });
  });
});
