import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { KitService } from './kit.service';

describe('KitService', () => {
  let service: KitService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(KitService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('envia paginação, filtros de texto, itens e inativos na listagem', () => {
    service
      .listar({
        page: 0,
        size: 10,
        sort: 'nome,asc',
        nome: '  Cesta Básica  ',
        descricao: '  Mensal ',
        itensIds: [10, 20],
        apenasInativos: true,
      })
      .subscribe();

    const requisicao = http.expectOne((request) => request.url === 'doacoes/kits');
    expect(requisicao.request.method).toBe('GET');
    expect(requisicao.request.params.get('page')).toBe('0');
    expect(requisicao.request.params.get('size')).toBe('10');
    expect(requisicao.request.params.get('sort')).toBe('nome,asc');
    expect(requisicao.request.params.get('nome')).toBe('Cesta Básica');
    expect(requisicao.request.params.get('descricao')).toBe('Mensal');
    expect(requisicao.request.params.getAll('itensIds')).toEqual(['10', '20']);
    expect(requisicao.request.params.get('apenasInativos')).toBe('true');
    requisicao.flush({
      dados: [],
      paginaAtual: 0,
      tamanhoPagina: 10,
      totalElementos: 0,
      totalPaginas: 0,
    });
  });

  it('chama listarTodos e consultarDisponibilidade com os parâmetros esperados', () => {
    service.listarTodos().subscribe();
    const reqTodos = http.expectOne((request) => request.url === 'doacoes/kits/todos');
    expect(reqTodos.request.method).toBe('GET');
    expect(reqTodos.request.params.get('incluirInativos')).toBe('false');
    reqTodos.flush([]);

    service.consultarDisponibilidade(5, 100).subscribe();
    const reqDisp = http.expectOne(
      (request) => request.url === 'doacoes/kits/5/disponibilidade',
    );
    expect(reqDisp.request.method).toBe('GET');
    expect(reqDisp.request.params.get('localArmazenamentoId')).toBe('100');
    reqDisp.flush({
      idKit: 5,
      nomeKit: 'Cesta Básica',
      localArmazenamentoId: 100,
      quantidadeDisponivel: 4,
      itens: [],
    });
  });

  it('usa os endpoints de cadastro, consulta, atualização e ciclo de vida', () => {
    service.buscarPorId(1).subscribe();
    const busca = http.expectOne('doacoes/kits/1');
    expect(busca.request.method).toBe('GET');
    busca.flush({
      id: 1,
      nome: 'Cesta Básica',
      totalItens: 1,
      quantidadeDistribuivel: 2,
      itens: [],
      ativo: true,
    });

    const dados = {
      nome: 'Cesta Básica',
      descricao: 'Cesta mensal',
      itens: [{ idItem: 1, quantidade: 2 }],
    };
    service.cadastrar(dados).subscribe();
    const cadastro = http.expectOne('doacoes/kits');
    expect(cadastro.request.method).toBe('POST');
    expect(cadastro.request.body).toEqual(dados);
    cadastro.flush({ id: 1, ...dados, totalItens: 1, quantidadeDistribuivel: 0, ativo: true });

    service.atualizar(1, dados).subscribe();
    const atualizacao = http.expectOne('doacoes/kits/1');
    expect(atualizacao.request.method).toBe('PUT');
    expect(atualizacao.request.body).toEqual(dados);
    atualizacao.flush({ id: 1, ...dados, totalItens: 1, quantidadeDistribuivel: 0, ativo: true });

    service.desativar(1).subscribe();
    const desativacao = http.expectOne('doacoes/kits/1/desativar');
    expect(desativacao.request.method).toBe('PATCH');
    desativacao.flush(null);

    service.reativar(1).subscribe();
    const reativacao = http.expectOne('doacoes/kits/1/reativar');
    expect(reativacao.request.method).toBe('PATCH');
    reativacao.flush(null);
  });
});
