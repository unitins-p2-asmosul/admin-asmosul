import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { MovimentacaoService } from './movimentacao.service';

describe('MovimentacaoService', () => {
  let service: MovimentacaoService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(MovimentacaoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('consulta listagem paginada com filtros, período e ordenação', () => {
    service
      .listar({
        page: 1,
        size: 10,
        sort: 'dataHora,desc',
        itemId: 3,
        localAntigoId: 4,
        localNovoId: 5,
        quantidade: 8,
        dataHoraInicio: '2026-10-20T08:30',
        dataHoraFim: '2026-10-21T17:00',
      })
      .subscribe();

    const requisicao = http.expectOne((request) => request.url === 'doacoes/movimentacoes');
    expect(requisicao.request.method).toBe('GET');
    expect(requisicao.request.params.get('page')).toBe('1');
    expect(requisicao.request.params.get('size')).toBe('10');
    expect(requisicao.request.params.get('sort')).toBe('dataHora,desc');
    expect(requisicao.request.params.get('itemId')).toBe('3');
    expect(requisicao.request.params.get('localAntigoId')).toBe('4');
    expect(requisicao.request.params.get('localNovoId')).toBe('5');
    expect(requisicao.request.params.get('quantidade')).toBe('8');
    expect(requisicao.request.params.get('dataHoraInicio')).toBe('2026-10-20T08:30');
    expect(requisicao.request.params.get('dataHoraFim')).toBe('2026-10-21T17:00');
    requisicao.flush({
      dados: [],
      paginaAtual: 1,
      tamanhoPagina: 10,
      totalElementos: 0,
      totalPaginas: 0,
    });
  });

  it('consulta por ID, cadastra e atualiza somente a descrição sem expor exclusão', () => {
    service.buscarPorId(12).subscribe();
    const busca = http.expectOne('doacoes/movimentacoes/12');
    expect(busca.request.method).toBe('GET');
    busca.flush({ id: 12, itens: [] });

    const requisicao = {
      idNovoArmazenamento: 7,
      descricao: 'Transferência',
      itens: [{ idItem: 2, idArmazenamentoAntigo: 3, quantidade: 5 }],
    };
    service.cadastrar(requisicao).subscribe();
    const cadastro = http.expectOne('doacoes/movimentacoes');
    expect(cadastro.request.method).toBe('POST');
    expect(cadastro.request.body).toEqual(requisicao);
    cadastro.flush({ id: 13, itens: [] });

    service.atualizar(13, { descricao: 'Descrição revisada' }).subscribe();
    const atualizacao = http.expectOne('doacoes/movimentacoes/13');
    expect(atualizacao.request.method).toBe('PATCH');
    expect(atualizacao.request.body).toEqual({ descricao: 'Descrição revisada' });
    atualizacao.flush({ id: 13, itens: [] });
  });
});
