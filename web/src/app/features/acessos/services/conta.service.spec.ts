import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { ContaRequisicao } from '../models/conta.model';
import { ContaService } from './conta.service';

describe('ContaService', () => {
  let service: ContaService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ContaService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('envia paginação, filtros e perfis múltiplos em GET /contas', () => {
    service.listar({
      page: 2,
      size: 10,
      sort: 'nomeUsuario,desc',
      nomePessoa: 'Ana',
      perfis: ['GERENCIADOR_PESSOAS', 'GERENCIADOR_ACESSO'],
      redefinirSenha: true,
      dataCriacao: '10-02-2026',
      apenasInativos: true,
    }).subscribe();

    const requisicao = http.expectOne((request) => request.url === 'contas');
    expect(requisicao.request.method).toBe('GET');
    expect(requisicao.request.params.get('page')).toBe('2');
    expect(requisicao.request.params.get('size')).toBe('10');
    expect(requisicao.request.params.get('sort')).toBe('nomeUsuario,desc');
    expect(requisicao.request.params.get('nomePessoa')).toBe('Ana');
    expect(requisicao.request.params.get('redefinirSenha')).toBe('true');
    expect(requisicao.request.params.get('dataCriacao')).toBe('10-02-2026');
    expect(requisicao.request.params.get('apenasInativos')).toBe('true');
    expect(requisicao.request.params.getAll('perfis')).toEqual([
      'GERENCIADOR_PESSOAS',
      'GERENCIADOR_ACESSO',
    ]);
    requisicao.flush({ dados: [], paginaAtual: 2, tamanhoPagina: 10, totalElementos: 0, totalPaginas: 0 });
  });

  it('busca uma conta e cadastra com o payload definido pela API', () => {
    service.buscarPorId(8).subscribe();
    const busca = http.expectOne('contas/8');
    expect(busca.request.method).toBe('GET');
    busca.flush({ id: 8 });

    const payload: ContaRequisicao = {
      pessoaId: 15,
      nomeUsuario: 'ana.silva',
      senhaTemporaria: 'Senha@123',
      perfis: ['GERENCIADOR_PESSOAS'],
    };
    service.cadastrar(payload).subscribe();
    const cadastro = http.expectOne('contas');
    expect(cadastro.request.method).toBe('POST');
    expect(cadastro.request.body).toEqual(payload);
    cadastro.flush({ id: 9 });
  });

  it('atualiza usuário e perfis em endpoints separados', () => {
    service.atualizar(9, { nomeUsuario: 'ana.nova' }).subscribe();
    const usuario = http.expectOne('contas/9');
    expect(usuario.request.method).toBe('PUT');
    expect(usuario.request.body).toEqual({ nomeUsuario: 'ana.nova' });
    usuario.flush({ id: 9 });

    service.atualizarPerfis(9, ['GERENCIADOR_ACESSO']).subscribe();
    const perfis = http.expectOne('contas/9/perfis');
    expect(perfis.request.method).toBe('PUT');
    expect(perfis.request.body).toEqual({ perfis: ['GERENCIADOR_ACESSO'] });
    perfis.flush({ id: 9 });
  });

  it('redefine senha e alterna estado usando apenas os endpoints administrativos', () => {
    service.redefinirSenhaAdmin(9, 'Nova@123').subscribe();
    const senha = http.expectOne('contas/9/redefinir-senha');
    expect(senha.request.method).toBe('PATCH');
    expect(senha.request.body).toEqual({ novaSenhaTemporaria: 'Nova@123' });
    senha.flush(null);

    service.desativar(9).subscribe();
    const desativar = http.expectOne('contas/9/desativar');
    expect(desativar.request.method).toBe('PATCH');
    expect(desativar.request.body).toBeNull();
    desativar.flush(null);

    service.reativar(9).subscribe();
    const reativar = http.expectOne('contas/9/reativar');
    expect(reativar.request.method).toBe('PATCH');
    reativar.flush(null);
  });

  it('lista os perfis disponíveis', () => {
    service.listarPerfis().subscribe();
    const requisicao = http.expectOne('contas/perfis');
    expect(requisicao.request.method).toBe('GET');
    requisicao.flush([{ codigo: 'GERENCIADOR_PESSOAS', descricao: 'Gerenciador de Pessoas' }]);
  });
});