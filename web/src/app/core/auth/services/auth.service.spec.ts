import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { describe, it, expect, beforeEach, afterEach } from 'vitest';
import { AuthService } from './auth.service';
import { CredenciaisLogin, LoginResposta, Perfil } from '../models/auth.model';

describe('AuthService', () => {
  let service: AuthService;
  let httpTesting: HttpTestingController;

  const TOKEN_VALIDO =
    'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2FvLnNpbHZhIiwiaWQiOjEwLCJwZXJmaXMiOlsiR0VSRU5DSUFET1JfUEVTU09BUyJdLCJleHAiOjE4OTM0NTYwMDAsImlzcyI6ImFzbW9zdWwtYXBpIn0.mock-sig';

  const TOKEN_EXPIRADO =
    'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2FvLnNpbHZhIiwiaWQiOjEwLCJwZXJmaXMiOlsiR0VSRU5DSUFET1JfUEVTU09BUyJdLCJleHAiOjEwMDAwMDAwMDAsImlzcyI6ImFzbW9zdWwtYXBpIn0.mock-sig';

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [AuthService, provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(AuthService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
    localStorage.clear();
  });

  it('deve ser instanciado e inicializar sem usuário logado quando o storage está vazio', () => {
    expect(service).toBeTruthy();
    expect(service.obterUsuarioAtual()).toBeNull();
    expect(service.obterToken()).toBeNull();
    expect(service.estaAutenticado()).toBe(false);
  });

  it('deve realizar login, salvar o token no localStorage e emitir o usuário autenticado', () => {
    const credenciais: CredenciaisLogin = {
      nomeUsuario: 'joao.silva',
      senha: 'password123',
    };

    const respostaApi: LoginResposta = {
      token: TOKEN_VALIDO,
      tipo: 'Bearer',
      expiracao: '2030-01-01T00:00:00Z',
      nomeUsuario: 'joao.silva',
      perfis: [{ codigo: Perfil.GERENCIADOR_PESSOAS, descricao: 'Gerenciador de Pessoas' }],
    };

    let usuarioEmitido = null;
    service.usuarioAtual$.subscribe((usuario) => {
      usuarioEmitido = usuario;
    });

    service.login(credenciais).subscribe((resposta) => {
      expect(resposta).toEqual(respostaApi);
    });

    const req = httpTesting.expectOne('auth/login');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(credenciais);
    req.flush(respostaApi);

    expect(localStorage.getItem('@asmosul:token')).toBe(TOKEN_VALIDO);
    expect(service.obterToken()).toBe(TOKEN_VALIDO);
    expect(service.estaAutenticado()).toBe(true);

    const usuarioAtual = service.obterUsuarioAtual();
    expect(usuarioAtual).toBeTruthy();
    expect(usuarioAtual?.id).toBe(10);
    expect(usuarioAtual?.nomeUsuario).toBe('joao.silva');
    expect(usuarioAtual?.perfis).toContain(Perfil.GERENCIADOR_PESSOAS);
    expect(usuarioEmitido).toEqual(usuarioAtual);
  });

  it('deve limpar token e estado do usuário ao executar logout', () => {
    localStorage.setItem('@asmosul:token', TOKEN_VALIDO);
    service['restaurarSessao']();

    expect(service.obterUsuarioAtual()).not.toBeNull();
    expect(service.obterToken()).toBe(TOKEN_VALIDO);

    service.logout();

    expect(localStorage.getItem('@asmosul:token')).toBeNull();
    expect(service.obterToken()).toBeNull();
    expect(service.obterUsuarioAtual()).toBeNull();
    expect(service.estaAutenticado()).toBe(false);
  });

  it('deve verificar permissões corretamente com temPerfil', () => {
    localStorage.setItem('@asmosul:token', TOKEN_VALIDO);
    service['restaurarSessao']();

    expect(service.temPerfil(Perfil.GERENCIADOR_PESSOAS)).toBe(true);
    expect(service.temPerfil(Perfil.GERENCIADOR_ACESSO)).toBe(false);
  });

  it('deve retornar false em temPerfil se o usuário não estiver autenticado', () => {
    expect(service.temPerfil(Perfil.GERENCIADOR_PESSOAS)).toBe(false);
  });

  it('deve indicar que o token expirado não está autenticado e remover dados inválidos', () => {
    localStorage.setItem('@asmosul:token', TOKEN_EXPIRADO);
    service['restaurarSessao']();

    expect(service.estaAutenticado()).toBe(false);
    expect(service.obterUsuarioAtual()).toBeNull();
    expect(localStorage.getItem('@asmosul:token')).toBeNull();
  });

  it('deve consultar flag redefinirSenha no endpoint de contas quando não estiver no usuário', () => {
    localStorage.setItem('@asmosul:token', TOKEN_VALIDO);
    service['restaurarSessao']();

    let resultado: boolean | undefined;
    service.consultarRedefinirSenha().subscribe((val) => {
      resultado = val;
    });

    const req = httpTesting.expectOne('contas/10');
    expect(req.request.method).toBe('GET');
    req.flush({ id: 10, redefinirSenha: true });

    expect(resultado).toBe(true);
    expect(service.obterUsuarioAtual()?.redefinirSenha).toBe(true);
  });

  it('deve retornar a flag redefinirSenha diretamente quando já conhecida no usuário', () => {
    localStorage.setItem('@asmosul:token', TOKEN_VALIDO);
    service['restaurarSessao']();
    service.atualizarStatusRedefinirSenha(false);

    let resultado: boolean | undefined;
    service.consultarRedefinirSenha().subscribe((val) => {
      resultado = val;
    });

    httpTesting.expectNone('contas/10');
    expect(resultado).toBe(false);
  });

  it('deve executar PATCH contas/minha-senha e atualizar status local para false', () => {
    localStorage.setItem('@asmosul:token', TOKEN_VALIDO);
    service['restaurarSessao']();
    service.atualizarStatusRedefinirSenha(true);

    let concluido = false;
    service
      .alterarMinhaSenha({ senhaAtual: 'Senha@123', novaSenha: 'NovaSenha@456' })
      .subscribe(() => {
        concluido = true;
      });

    const req = httpTesting.expectOne('acessos/contas/minha-senha');
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ senhaAtual: 'Senha@123', novaSenha: 'NovaSenha@456' });
    req.flush(null, { status: 204, statusText: 'No Content' });

    expect(concluido).toBe(true);
    expect(service.obterUsuarioAtual()?.redefinirSenha).toBe(false);
  });
});
