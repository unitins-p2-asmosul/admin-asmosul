import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { EnderecoArmazenamentoService } from './endereco-armazenamento.service';

describe('EnderecoArmazenamentoService', () => {
  let service: EnderecoArmazenamentoService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(EnderecoArmazenamentoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('envia paginação, filtros de texto e somente inativos na listagem', () => {
    service
      .listar({
        page: 1,
        size: 10,
        sort: 'nome,desc',
        nome: '  Galpão  ',
        bairro: '  Centro  ',
        logradouro: '  Avenida  ',
        apenasInativos: true,
      })
      .subscribe();

    const requisicao = http.expectOne((request) => request.url === 'doacoes/enderecos');
    expect(requisicao.request.method).toBe('GET');
    expect(requisicao.request.params.get('page')).toBe('1');
    expect(requisicao.request.params.get('size')).toBe('10');
    expect(requisicao.request.params.get('sort')).toBe('nome,desc');
    expect(requisicao.request.params.get('nome')).toBe('Galpão');
    expect(requisicao.request.params.get('bairro')).toBe('Centro');
    expect(requisicao.request.params.get('logradouro')).toBe('Avenida');
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
    const busca = http.expectOne('doacoes/enderecos/4');
    expect(busca.request.method).toBe('GET');
    busca.flush({ id: 4 });

    const dados = {
      nome: 'Galpão Central',
      cep: '77000-000',
      uf: 'TO',
      cidade: 'Palmas',
      bairro: 'Centro',
      logradouro: 'Avenida JK',
      numero: '100',
    };
    service.cadastrar(dados).subscribe();
    const cadastro = http.expectOne('doacoes/enderecos');
    expect(cadastro.request.method).toBe('POST');
    expect(cadastro.request.body).toEqual(dados);
    cadastro.flush({ id: 4 });

    service.atualizar(4, dados).subscribe();
    const atualizacao = http.expectOne('doacoes/enderecos/4');
    expect(atualizacao.request.method).toBe('PUT');
    expect(atualizacao.request.body).toEqual(dados);
    atualizacao.flush({ id: 4 });

    service.desativar(4).subscribe();
    const desativacao = http.expectOne('doacoes/enderecos/4/desativar');
    expect(desativacao.request.method).toBe('PATCH');
    desativacao.flush(null);

    service.reativar(4).subscribe();
    const reativacao = http.expectOne('doacoes/enderecos/4/reativar');
    expect(reativacao.request.method).toBe('PATCH');
    reativacao.flush(null);
  });
});
