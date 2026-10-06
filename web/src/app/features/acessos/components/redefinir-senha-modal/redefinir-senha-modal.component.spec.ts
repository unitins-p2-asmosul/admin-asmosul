import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { MatDialogRef } from '@angular/material/dialog';
import { AuthService } from '@core/auth/services/auth.service';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { of, throwError } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { RedefinirSenhaModalComponent } from './redefinir-senha-modal.component';

describe('RedefinirSenhaModalComponent', () => {
  let component: RedefinirSenhaModalComponent;
  let fixture: ComponentFixture<RedefinirSenhaModalComponent>;

  const authServiceMock = {
    alterarMinhaSenha: vi.fn(),
  };

  const notificacaoServiceMock = {
    sucesso: vi.fn(),
    alerta: vi.fn(),
    erro: vi.fn(),
  };

  const dialogRefMock = {
    close: vi.fn(),
  };

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [RedefinirSenhaModalComponent],
      providers: [
        { provide: AuthService, useValue: authServiceMock },
        { provide: NotificacaoService, useValue: notificacaoServiceMock },
        { provide: MatDialogRef, useValue: dialogRefMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RedefinirSenhaModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('deve ser instanciado e inicializar com formulário inválido', () => {
    expect(component).toBeTruthy();
    expect(component['form'].valid).toBe(false);
  });

  it('deve validar campos obrigatórios', () => {
    const form = component['form'];
    expect(form.controls.senhaAtual.hasError('required')).toBe(true);
    expect(form.controls.novaSenha.hasError('required')).toBe(true);
    expect(form.controls.confirmarNovaSenha.hasError('required')).toBe(true);
  });

  it('deve apontar erro de senhasDiferentes quando a nova senha não coincide com a confirmação', () => {
    const form = component['form'];
    form.patchValue({
      senhaAtual: 'SenhaAtual@123',
      novaSenha: 'NovaSenha@123',
      confirmarNovaSenha: 'OutraSenha@123',
    });

    expect(form.hasError('senhasDiferentes')).toBe(true);
    expect(form.valid).toBe(false);
  });

  it('deve validar formulário como válido quando todos os campos estiverem preenchidos e senhas coincidirem', () => {
    const form = component['form'];
    form.patchValue({
      senhaAtual: 'SenhaAtual@123',
      novaSenha: 'NovaSenha@123',
      confirmarNovaSenha: 'NovaSenha@123',
    });

    expect(form.hasError('senhasDiferentes')).toBe(false);
    expect(form.valid).toBe(true);
  });

  it('deve alternar a visibilidade das senhas', () => {
    expect(component['mostrarSenhaAtual']()).toBe(false);
    component['alternarVisibilidadeSenhaAtual']();
    expect(component['mostrarSenhaAtual']()).toBe(true);

    expect(component['mostrarNovaSenha']()).toBe(false);
    component['alternarVisibilidadeNovaSenha']();
    expect(component['mostrarNovaSenha']()).toBe(true);

    expect(component['mostrarConfirmarNovaSenha']()).toBe(false);
    component['alternarVisibilidadeConfirmarNovaSenha']();
    expect(component['mostrarConfirmarNovaSenha']()).toBe(true);
  });

  it('deve impedir a propagação do evento ao pressionar Escape', () => {
    const event = new KeyboardEvent('keydown', { key: 'Escape' });
    const preventDefaultSpy = vi.spyOn(event, 'preventDefault');
    const stopPropagationSpy = vi.spyOn(event, 'stopPropagation');

    component['evitarEscape'](event);

    expect(preventDefaultSpy).toHaveBeenCalled();
    expect(stopPropagationSpy).toHaveBeenCalled();
  });

  it('não deve enviar quando o formulário for inválido', () => {
    component['salvarNovaSenha']();
    expect(authServiceMock.alterarMinhaSenha).not.toHaveBeenCalled();
  });

  it('deve chamar alterarMinhaSenha e emitir sucesso quando a API responder com status 204', () => {
    authServiceMock.alterarMinhaSenha.mockReturnValue(of(void 0));
    let sucessoEmitido = false;
    component.sucesso.subscribe(() => {
      sucessoEmitido = true;
    });

    component['form'].patchValue({
      senhaAtual: 'SenhaAtual@123',
      novaSenha: 'NovaSenha@456',
      confirmarNovaSenha: 'NovaSenha@456',
    });

    component['salvarNovaSenha']();

    expect(authServiceMock.alterarMinhaSenha).toHaveBeenCalledWith({
      senhaAtual: 'SenhaAtual@123',
      novaSenha: 'NovaSenha@456',
    });
    expect(notificacaoServiceMock.sucesso).toHaveBeenCalledWith('Senha redefinida com sucesso.');
    expect(sucessoEmitido).toBe(true);
    expect(dialogRefMock.close).toHaveBeenCalledWith(true);
  });

  it('deve exibir mensagem de erro dentro do modal ao receber erro 400 da API sem fechá-lo', () => {
    const erroHttp = new HttpErrorResponse({
      status: 400,
      error: { detail: 'A senha atual está incorreta.' },
    });
    authServiceMock.alterarMinhaSenha.mockReturnValue(throwError(() => erroHttp));

    component['form'].patchValue({
      senhaAtual: 'SenhaErrada@123',
      novaSenha: 'NovaSenha@456',
      confirmarNovaSenha: 'NovaSenha@456',
    });

    component['salvarNovaSenha']();
    fixture.detectChanges();

    expect(component['enviando']()).toBe(false);
    expect(component['mensagemErro']()).toBe('A senha atual está incorreta.');
    expect(dialogRefMock.close).not.toHaveBeenCalled();

    const alerta = fixture.debugElement.query(By.css('[role="alert"]'));
    expect(alerta).toBeTruthy();
    expect(alerta.nativeElement.textContent).toContain('A senha atual está incorreta.');
  });
});
