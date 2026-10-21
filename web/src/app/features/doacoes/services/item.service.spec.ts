import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { UnidadeMedidaCodigo } from '../models/unidade-medida.model';
import { ItemService } from './item.service';

describe('ItemService', () => {
  let service: ItemService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ItemService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('envia paginação, filtros de texto e inativos na listagem', () => {
    service
      .listar({
        page: 0,
        size: 10,
        sort: 'nome,asc',
        nome: '  Arroz  ',
        categoriasIds: [1, 2],
        estoque: 10,
        precoUnitarioMinimo: 100,
        precoUnitarioMaximo: 500,
        apenasInativos: true,
      })
      .subscribe();

    const requisicao = http.expectOne((request) => request.url === 'doacoes/itens');
    expect(requisicao.request.method).toBe('GET');
    expect(requisicao.request.params.get('page')).toBe('0');
    expect(requisicao.request.params.get('size')).toBe('10');
    expect(requisicao.request.params.get('sort')).toBe('nome,asc');
    expect(requisicao.request.params.get('nome')).toBe('Arroz');
    expect(requisicao.request.params.getAll('categoriasIds')).toEqual(['1', '2']);
    expect(requisicao.request.params.get('estoque')).toBe('10');
    expect(requisicao.request.params.get('precoUnitarioMinimo')).toBe('100');
    expect(requisicao.request.params.get('precoUnitarioMaximo')).toBe('500');
    expect(requisicao.request.params.get('apenasInativos')).toBe('true');
    requisicao.flush({
      dados: [],
      paginaAtual: 0,
      tamanhoPagina: 10,
      totalElementos: 0,
      totalPaginas: 0,
    });
  });

  it('chama listarTodos sem e com inclusão de inativos', () => {
    service.listarTodos().subscribe();
    const req1 = http.expectOne((request) => request.url === 'doacoes/itens/todos');
    expect(req1.request.method).toBe('GET');
    expect(req1.request.params.get('incluirInativos')).toBe('false');
    req1.flush([]);

    service.listarTodos(true).subscribe();
    const req2 = http.expectOne((request) => request.url === 'doacoes/itens/todos');
    expect(req2.request.method).toBe('GET');
    expect(req2.request.params.get('incluirInativos')).toBe('true');
    req2.flush([]);
  });

  it('usa os endpoints de cadastro, consulta, atualização e ciclo de vida', () => {
    service.buscarPorId(1).subscribe();
    const busca = http.expectOne('doacoes/itens/1');
    expect(busca.request.method).toBe('GET');
    busca.flush({ id: 1, nome: 'Feijão Carioca', locais: [] });

    const dados = {
      nome: 'Feijão Carioca',
      idCategoria: 1,
      precoUnitario: 800,
      unidadeMedida: UnidadeMedidaCodigo.PACOTE,
      descricao: '1kg',
    };
    service.cadastrar(dados).subscribe();
    const cadastro = http.expectOne('doacoes/itens');
    expect(cadastro.request.method).toBe('POST');
    expect(cadastro.request.body).toEqual(dados);
    cadastro.flush({ id: 1, ...dados });

    service.atualizar(1, dados).subscribe();
    const atualizacao = http.expectOne('doacoes/itens/1');
    expect(atualizacao.request.method).toBe('PUT');
    expect(atualizacao.request.body).toEqual(dados);
    atualizacao.flush({ id: 1, ...dados });

    service.desativar(1).subscribe();
    const desativacao = http.expectOne('doacoes/itens/1/desativar');
    expect(desativacao.request.method).toBe('PATCH');
    desativacao.flush(null);

    service.reativar(1).subscribe();
    const reativacao = http.expectOne('doacoes/itens/1/reativar');
    expect(reativacao.request.method).toBe('PATCH');
    reativacao.flush(null);
  });
});
