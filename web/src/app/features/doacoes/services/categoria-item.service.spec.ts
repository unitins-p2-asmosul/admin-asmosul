import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { CategoriaItemService } from './categoria-item.service';

describe('CategoriaItemService', () => {
  let service: CategoriaItemService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(CategoriaItemService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('envia paginação, filtros de texto e somente inativos na listagem', () => {
    service
      .listar({
        page: 1,
        size: 10,
        sort: 'nome,desc',
        nome: '  Alimentos  ',
        descricao: '  Cestas  ',
        apenasInativos: true,
      })
      .subscribe();

    const requisicao = http.expectOne((request) => request.url === 'doacoes/categorias');
    expect(requisicao.request.method).toBe('GET');
    expect(requisicao.request.params.get('page')).toBe('1');
    expect(requisicao.request.params.get('size')).toBe('10');
    expect(requisicao.request.params.get('sort')).toBe('nome,desc');
    expect(requisicao.request.params.get('nome')).toBe('Alimentos');
    expect(requisicao.request.params.get('descricao')).toBe('Cestas');
    expect(requisicao.request.params.get('apenasInativos')).toBe('true');
    requisicao.flush({
      dados: [],
      paginaAtual: 1,
      tamanhoPagina: 10,
      totalElementos: 0,
      totalPaginas: 0,
    });
  });

  it('usa os endpoints de cadastro, consulta, atualização e ciclo de vida', () => {
    service.buscarPorId(4).subscribe();
    const busca = http.expectOne('doacoes/categorias/4');
    expect(busca.request.method).toBe('GET');
    busca.flush({ id: 4 });

    const dados = { nome: 'Alimentos', descricao: 'Mantimentos' };
    service.cadastrar(dados).subscribe();
    const cadastro = http.expectOne('doacoes/categorias');
    expect(cadastro.request.method).toBe('POST');
    expect(cadastro.request.body).toEqual(dados);
    cadastro.flush({ id: 4 });

    service.atualizar(4, dados).subscribe();
    const atualizacao = http.expectOne('doacoes/categorias/4');
    expect(atualizacao.request.method).toBe('PUT');
    expect(atualizacao.request.body).toEqual(dados);
    atualizacao.flush({ id: 4 });

    service.desativar(4).subscribe();
    const desativacao = http.expectOne('doacoes/categorias/4/desativar');
    expect(desativacao.request.method).toBe('PATCH');
    desativacao.flush(null);

    service.reativar(4).subscribe();
    const reativacao = http.expectOne('doacoes/categorias/4/reativar');
    expect(reativacao.request.method).toBe('PATCH');
    reativacao.flush(null);
  });
});
