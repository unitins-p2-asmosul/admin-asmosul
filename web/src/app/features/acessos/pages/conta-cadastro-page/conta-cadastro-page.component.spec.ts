import { Location } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { DialogoConfirmacaoService } from '@features/shared/services/dialogo-confirmacao.service';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { of } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { PessoaService } from '@features/pessoas/services/pessoa.service';
import { ContaDetalhe, PerfilDisponivel } from '../../models/conta.model';
import { ContaService } from '../../services/conta.service';
import { ContaCadastroPageComponent } from './conta-cadastro-page.component';

const PERFIS: PerfilDisponivel[] = [
  { codigo: 'GERENCIADOR_PESSOAS', descricao: 'Gerenciador de Pessoas' },
];

const CONTA: ContaDetalhe = {
  id: 12,
  pessoaId: 1,
  nomePessoa: 'Ana Silva',
  email: 'ana@example.com',
  nomeUsuario: 'ana.silva',
  perfis: PERFIS,
  redefinirSenha: true,
  ativo: true,
  dataCriacao: '14-05-2026',
};

describe('ContaCadastroPageComponent', () => {
  let fixture: ComponentFixture<ContaCadastroPageComponent>;
  let contaServiceMock: Record<string, ReturnType<typeof vi.fn>>;
  let pessoaServiceMock: { listar: ReturnType<typeof vi.fn> };
  let notificacaoMock: {
    sucesso: ReturnType<typeof vi.fn>;
    alerta: ReturnType<typeof vi.fn>;
    erro: ReturnType<typeof vi.fn>;
  };
  let locationMock: { back: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    contaServiceMock = {
      listarPerfis: vi.fn().mockReturnValue(of(PERFIS)),
      buscarPorId: vi.fn().mockReturnValue(of(CONTA)),
      cadastrar: vi.fn().mockReturnValue(of(CONTA)),
      atualizar: vi.fn().mockReturnValue(of(CONTA)),
      atualizarPerfis: vi.fn().mockReturnValue(of(CONTA)),
      redefinirSenhaAdmin: vi.fn().mockReturnValue(of(void 0)),
      desativar: vi.fn().mockReturnValue(of(void 0)),
      reativar: vi.fn().mockReturnValue(of(void 0)),
    };
    pessoaServiceMock = {
      listar: vi.fn().mockReturnValue(of({
        dados: [
          { id: 1, nome: 'Ana Silva', tipoPessoa: 'FISICA', ativo: true },
          { id: 2, nome: 'Pessoa inativa', tipoPessoa: 'FISICA', ativo: false },
          { id: 3, nome: 'Empresa', tipoPessoa: 'JURIDICA', ativo: true },
        ],
        paginaAtual: 0,
        tamanhoPagina: 100,
        totalElementos: 3,
        totalPaginas: 1,
      })),
    };
    notificacaoMock = { sucesso: vi.fn(), alerta: vi.fn(), erro: vi.fn() };
    locationMock = { back: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [ContaCadastroPageComponent],
      providers: [
        { provide: ContaService, useValue: contaServiceMock },
        { provide: PessoaService, useValue: pessoaServiceMock },
        { provide: NotificacaoService, useValue: notificacaoMock },
        { provide: DialogoConfirmacaoService, useValue: { confirmar: vi.fn().mockResolvedValue(true) } },
        { provide: Location, useValue: locationMock },
        { provide: Router, useValue: { navigate: vi.fn() } },
        { provide: ActivatedRoute, useValue: { snapshot: { queryParams: { page: '2' } } } },
        { provide: 'MatDialog', useValue: { open: vi.fn() } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ContaCadastroPageComponent);
    fixture.detectChanges();
  });

  it('carrega todas as pessoas físicas ativas para a criação', () => {
    const component = fixture.componentInstance;

    expect(pessoaServiceMock.listar).toHaveBeenCalledWith({
      page: 0,
      size: 100,
      sort: 'nome,asc',
      tipoPessoa: 'FISICA',
    });
    expect(component['pessoas']().map((pessoa) => pessoa.nome)).toEqual(['Ana Silva']);
  });

  it('bloqueia cadastro inválido e sinaliza divergência entre senhas', () => {
    const component = fixture.componentInstance;
    component['form'].patchValue({
      pessoaId: 1,
      nomeUsuario: 'ana.silva',
      senhaTemporaria: 'Senha@123',
      confirmarSenha: 'Outra@123',
      perfis: ['GERENCIADOR_PESSOAS'],
    });

    component['salvar']();

    expect(component['form'].hasError('senhasDiferentes')).toBe(true);
    expect(contaServiceMock['cadastrar']).not.toHaveBeenCalled();
    expect(notificacaoMock.alerta).toHaveBeenCalledOnce();
  });

  it('renderiza a divergência de senha como erro no campo de confirmação', () => {
    const component = fixture.componentInstance;
    component['form'].patchValue({
      senhaTemporaria: 'Senha@123',
      confirmarSenha: 'Outra@123',
    });
    component['form'].controls.confirmarSenha.markAsTouched();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('As senhas não coincidem.');
  });

  it('cadastra conta com usuário aparado, senha temporária e perfis', () => {
    const component = fixture.componentInstance;
    component['form'].patchValue({
      pessoaId: 1,
      nomeUsuario: '  ana.silva  ',
      senhaTemporaria: 'Senha@123',
      confirmarSenha: 'Senha@123',
      perfis: ['GERENCIADOR_PESSOAS'],
    });

    component['salvar']();

    expect(contaServiceMock['cadastrar']).toHaveBeenCalledWith({
      pessoaId: 1,
      nomeUsuario: 'ana.silva',
      senhaTemporaria: 'Senha@123',
      perfis: ['GERENCIADOR_PESSOAS'],
    });
    expect(notificacaoMock.sucesso).toHaveBeenCalledWith('Conta cadastrada com sucesso.');
    expect(locationMock.back).toHaveBeenCalledOnce();
  });

  it('edita somente nome de usuário e perfis, mantendo pessoa sem edição', () => {
    fixture.componentRef.setInput('id', '12');
    fixture.detectChanges();
    const component = fixture.componentInstance;
    expect(contaServiceMock['buscarPorId']).toHaveBeenCalledWith(12);
    expect(component['form'].controls.pessoaId.disabled).toBe(true);
    expect(component['form'].controls.senhaTemporaria.validator).toBeNull();

    component['form'].patchValue({
      nomeUsuario: 'ana.nova',
      perfis: ['GERENCIADOR_PESSOAS'],
    });
    component['salvar']();

    expect(contaServiceMock['atualizar']).toHaveBeenCalledWith(12, { nomeUsuario: 'ana.nova' });
    expect(contaServiceMock['atualizarPerfis']).toHaveBeenCalledWith(12, ['GERENCIADOR_PESSOAS']);
    expect(contaServiceMock['cadastrar']).not.toHaveBeenCalled();
  });

  it('desabilita todos os campos em visualização e não permite salvar', () => {
    fixture.componentRef.setInput('id', '12');
    fixture.componentRef.setInput('visualizacao', true);
    fixture.detectChanges();
    const component = fixture.componentInstance;

    expect(component['form'].disabled).toBe(true);
    expect(component['registro']()?.nomePessoa).toBe('Ana Silva');
    component['salvar']();
    expect(contaServiceMock['atualizar']).not.toHaveBeenCalled();
    expect(contaServiceMock['cadastrar']).not.toHaveBeenCalled();
  });

  it('mapeia erros 400 para campos e conflitos 409 para usuário ou pessoa', () => {
    const component = fixture.componentInstance;
    component['tratarErro'](
      new HttpErrorResponse({
        status: 400,
        error: { invalidFields: { pessoaId: 'A pessoa está inativa.' } },
      }),
    );
    expect(component['form'].controls.pessoaId.getError('backend')).toBe('A pessoa está inativa.');

    component['tratarErro'](
      new HttpErrorResponse({ status: 409, error: { detail: 'Já existe uma conta com este nome de usuário.' } }),
    );
    expect(component['form'].controls.nomeUsuario.getError('backend')).toContain('nome de usuário');

    component['tratarErro'](
      new HttpErrorResponse({ status: 409, error: { detail: 'Esta pessoa já possui uma conta vinculada.' } }),
    );
    expect(component['form'].controls.pessoaId.getError('backend')).toContain('pessoa');
  });
});