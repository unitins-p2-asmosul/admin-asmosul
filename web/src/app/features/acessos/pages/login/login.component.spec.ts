import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { AuthService } from '@core/auth/services/auth.service';
import { LoginResposta } from '@core/auth/models/auth.model';
import { of, throwError } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { LoginComponent } from './login.component';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;

  const authServiceMock = {
    login: vi.fn(),
    consultarRedefinirSenha: vi.fn(),
    alterarMinhaSenha: vi.fn(),
  };

  const routerMock = {
    navigate: vi.fn(),
  };

  const respostaLoginPadrao: LoginResposta = {
    token: 'jwt-mock-token',
    tipo: 'Bearer',
    expiracao: '2030-01-01T00:00:00Z',
    nomeUsuario: 'asmosul',
    perfis: ['GERENCIADOR_ACESSO'],
    redefinirSenha: false,
  };

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        { provide: Router, useValue: routerMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('deve inicializar com o formulário inválido', () => {
    expect(component).toBeTruthy();
    expect(component['form'].valid).toBe(false);
  });

  it('deve validar obrigatoriedade de nomeUsuario e senha', () => {
    const form = component['form'];
    expect(form.controls.nomeUsuario.hasError('required')).toBe(true);
    expect(form.controls.senha.hasError('required')).toBe(true);

    form.patchValue({
      nomeUsuario: 'asmosul',
      senha: 'Asmosul@1234',
    });

    expect(form.valid).toBe(true);
  });

  it('deve alternar a visibilidade da senha ao clicar no botão', () => {
    expect(component['mostrarSenha']()).toBe(false);
    component['alternarVisibilidadeSenha']();
    expect(component['mostrarSenha']()).toBe(true);
    component['alternarVisibilidadeSenha']();
    expect(component['mostrarSenha']()).toBe(false);
  });

  it('não deve enviar a requisição quando o formulário for inválido', () => {
    component['entrar']();
    expect(authServiceMock.login).not.toHaveBeenCalled();
  });

  it('deve logar e navegar para rota principal quando redefinirSenha for false', () => {
    authServiceMock.login.mockReturnValue(of(respostaLoginPadrao));

    component['form'].patchValue({
      nomeUsuario: 'asmosul',
      senha: 'Asmosul@1234',
    });

    component['entrar']();

    expect(authServiceMock.login).toHaveBeenCalledWith({
      nomeUsuario: 'asmosul',
      senha: 'Asmosul@1234',
    });
    expect(component['exibirModalRedefinir']()).toBe(false);
    expect(routerMock.navigate).toHaveBeenCalledWith(['/']);
  });

  it('deve abrir modal de redefinição de senha quando redefinirSenha for true na resposta', () => {
    const respostaPrimeiroAcesso: LoginResposta = {
      ...respostaLoginPadrao,
      redefinirSenha: true,
    };
    authServiceMock.login.mockReturnValue(of(respostaPrimeiroAcesso));

    component['form'].patchValue({
      nomeUsuario: 'novo.usuario',
      senha: 'SenhaTemporaria@123',
    });

    component['entrar']();
    fixture.detectChanges();

    expect(authServiceMock.login).toHaveBeenCalledWith({
      nomeUsuario: 'novo.usuario',
      senha: 'SenhaTemporaria@123',
    });
    expect(component['exibirModalRedefinir']()).toBe(true);
    expect(routerMock.navigate).not.toHaveBeenCalled();

    // Modal deve estar presente no DOM
    const modalElement = fixture.debugElement.query(By.css('app-redefinir-senha-modal'));
    expect(modalElement).toBeTruthy();
  });

  it('deve consultar flag via consultarRedefinirSenha quando não estiver presente na resposta', () => {
    const respostaSemFlag: LoginResposta = {
      ...respostaLoginPadrao,
      redefinirSenha: undefined,
    };
    authServiceMock.login.mockReturnValue(of(respostaSemFlag));
    authServiceMock.consultarRedefinirSenha.mockReturnValue(of(true));

    component['form'].patchValue({
      nomeUsuario: 'outro.usuario',
      senha: 'Senha@123',
    });

    component['entrar']();

    expect(authServiceMock.consultarRedefinirSenha).toHaveBeenCalled();
    expect(component['exibirModalRedefinir']()).toBe(true);
    expect(routerMock.navigate).not.toHaveBeenCalled();
  });

  it('deve fechar o modal e navegar para rota principal após conclusão com sucesso', () => {
    component['exibirModalRedefinir'].set(true);

    component['aoConcluirRedefinicao']();

    expect(component['exibirModalRedefinir']()).toBe(false);
    expect(routerMock.navigate).toHaveBeenCalledWith(['/']);
  });

  it('deve exibir mensagem de erro clara em caso de erro 401', () => {
    const erro401 = new HttpErrorResponse({ status: 401 });
    authServiceMock.login.mockReturnValue(throwError(() => erro401));

    component['form'].patchValue({
      nomeUsuario: 'usuario.incorreto',
      senha: 'senha-errada',
    });

    component['entrar']();
    fixture.detectChanges();

    expect(component['carregando']()).toBe(false);
    expect(component['mensagemErro']()).toBe(
      'Nome de usuário ou senha incorretos, ou conta inativa.',
    );

    const alerta = fixture.debugElement.query(By.css('[role="alert"]'));
    expect(alerta).toBeTruthy();
    expect(alerta.nativeElement.textContent).toContain(
      'Nome de usuário ou senha incorretos, ou conta inativa.',
    );
  });

  it('deve exibir mensagem retornada pela API em caso de erro 400', () => {
    const erro400 = new HttpErrorResponse({
      status: 400,
      error: { detail: 'Dados de entrada inválidos.' },
    });
    authServiceMock.login.mockReturnValue(throwError(() => erro400));

    component['form'].patchValue({
      nomeUsuario: 'usuario',
      senha: '123',
    });

    component['entrar']();
    fixture.detectChanges();

    expect(component['carregando']()).toBe(false);
    expect(component['mensagemErro']()).toBe('Dados de entrada inválidos.');
  });

  it('deve exibir mensagem amigável em caso de erro de conexão (status 0)', () => {
    const erroConexao = new HttpErrorResponse({ status: 0 });
    authServiceMock.login.mockReturnValue(throwError(() => erroConexao));

    component['form'].patchValue({
      nomeUsuario: 'usuario',
      senha: '123',
    });

    component['entrar']();
    fixture.detectChanges();

    expect(component['carregando']()).toBe(false);
    expect(component['mensagemErro']()).toBe(
      'Não foi possível conectar ao servidor. Verifique sua conexão.',
    );
  });
});
