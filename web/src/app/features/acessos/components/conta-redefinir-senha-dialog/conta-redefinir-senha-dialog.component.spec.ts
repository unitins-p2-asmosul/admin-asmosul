import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { of, Subject, throwError } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ContaService } from '../../services/conta.service';
import {
  ContaRedefinirSenhaDialogComponent,
  ContaRedefinirSenhaDialogData,
} from './conta-redefinir-senha-dialog.component';

describe('ContaRedefinirSenhaDialogComponent', () => {
  let contaServiceMock: { redefinirSenhaAdmin: ReturnType<typeof vi.fn> };
  let notificacaoMock: {
    sucesso: ReturnType<typeof vi.fn>;
    erro: ReturnType<typeof vi.fn>;
  };
  let dialogRefMock: { close: ReturnType<typeof vi.fn> };
  let fixture: ReturnType<typeof TestBed.createComponent<ContaRedefinirSenhaDialogComponent>>;
  let data: ContaRedefinirSenhaDialogData;

  beforeEach(async () => {
    contaServiceMock = { redefinirSenhaAdmin: vi.fn().mockReturnValue(of(void 0)) };
    notificacaoMock = { sucesso: vi.fn(), erro: vi.fn() };
    dialogRefMock = { close: vi.fn() };
    data = { contaId: 24, nomePessoa: 'Ana Silva', nomeUsuario: 'ana.silva' };

    await TestBed.configureTestingModule({
      imports: [ContaRedefinirSenhaDialogComponent],
      providers: [
        { provide: ContaService, useValue: contaServiceMock },
        { provide: NotificacaoService, useValue: notificacaoMock },
        { provide: MAT_DIALOG_DATA, useValue: data },
        { provide: MatDialogRef, useValue: dialogRefMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ContaRedefinirSenhaDialogComponent);
    fixture.detectChanges();
  });

  it('não permite confirmar senhas vazias ou diferentes', () => {
    const component = fixture.componentInstance;
    component['form'].setValue({ novaSenhaTemporaria: 'Nova@123', confirmarNovaSenha: 'Outra@123' });

    component['confirmar']();

    expect(component['form'].hasError('senhasDiferentes')).toBe(true);
    expect(contaServiceMock.redefinirSenhaAdmin).not.toHaveBeenCalled();
    expect(component['enviando']()).toBe(false);
  });

  it('envia somente a nova senha e fecha com sucesso', () => {
    const component = fixture.componentInstance;
    component['form'].setValue({ novaSenhaTemporaria: 'Nova@123', confirmarNovaSenha: 'Nova@123' });

    component['confirmar']();

    expect(contaServiceMock.redefinirSenhaAdmin).toHaveBeenCalledWith(24, 'Nova@123');
    expect(notificacaoMock.sucesso).toHaveBeenCalledWith('Senha temporária redefinida com sucesso.');
    expect(dialogRefMock.close).toHaveBeenCalledWith(true);
  });

  it('notifica falha da API e libera o formulário novamente', () => {
    contaServiceMock.redefinirSenhaAdmin.mockReturnValue(
      throwError(() => ({ error: { detail: 'Conta inativa.' } })),
    );
    const component = fixture.componentInstance;
    component['form'].setValue({ novaSenhaTemporaria: 'Nova@123', confirmarNovaSenha: 'Nova@123' });

    component['confirmar']();

    expect(notificacaoMock.erro).toHaveBeenCalledWith('Conta inativa.');
    expect(component['enviando']()).toBe(false);
    expect(dialogRefMock.close).not.toHaveBeenCalled();
  });

  it('ignora uma segunda confirmação enquanto a requisição está pendente', () => {
    const requisicao = new Subject<void>();
    contaServiceMock.redefinirSenhaAdmin.mockReturnValue(requisicao);
    const component = fixture.componentInstance;
    component['form'].setValue({ novaSenhaTemporaria: 'Nova@123', confirmarNovaSenha: 'Nova@123' });

    component['confirmar']();
    component['confirmar']();

    expect(contaServiceMock.redefinirSenhaAdmin).toHaveBeenCalledOnce();
    requisicao.next();
    requisicao.complete();
    expect(dialogRefMock.close).toHaveBeenCalledWith(true);
  });
});