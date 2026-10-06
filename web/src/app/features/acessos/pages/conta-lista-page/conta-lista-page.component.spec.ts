import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, convertToParamMap, Router } from '@angular/router';
import { DialogoConfirmacaoService } from '@features/shared/services/dialogo-confirmacao.service';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { BehaviorSubject, of } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ContaFiltroDialogComponent } from '../../components/conta-filtro-dialog/conta-filtro-dialog.component';
import { ContaRedefinirSenhaDialogComponent } from '../../components/conta-redefinir-senha-dialog/conta-redefinir-senha-dialog.component';
import { ContaResumo } from '../../models/conta.model';
import { ContaService } from '../../services/conta.service';
import { ContaListaPageComponent } from './conta-lista-page.component';

const CONTA: ContaResumo = {
  id: 4,
  nomePessoa: 'Ana Silva',
  email: 'ana@example.com',
  nomeUsuario: 'ana.silva',
  perfis: [{ codigo: 'GERENCIADOR_PESSOAS', descricao: 'Gerenciador de Pessoas' }],
  redefinirSenha: true,
  ativo: true,
  dataCriacao: '14-05-2026',
};

describe('ContaListaPageComponent', () => {
  let fixture: ComponentFixture<ContaListaPageComponent>;
  let contaServiceMock: Record<string, ReturnType<typeof vi.fn>>;
  let dialogMock: { open: ReturnType<typeof vi.fn> };
  let routerMock: { navigate: ReturnType<typeof vi.fn> };
  let confirmarMock: ReturnType<typeof vi.fn>;
  let notificacaoMock: { sucesso: ReturnType<typeof vi.fn> };
  let parametrosUrl: BehaviorSubject<ReturnType<typeof convertToParamMap>>;

  beforeEach(async () => {
    parametrosUrl = new BehaviorSubject(convertToParamMap({}));
    contaServiceMock = {
      listar: vi.fn().mockReturnValue(of({
        dados: [],
        paginaAtual: 0,
        tamanhoPagina: 10,
        totalElementos: 0,
        totalPaginas: 0,
      })),
      desativar: vi.fn().mockReturnValue(of(void 0)),
      reativar: vi.fn().mockReturnValue(of(void 0)),
    };
    dialogMock = {
      open: vi.fn().mockReturnValue({ afterClosed: () => of(undefined) }),
    };
    routerMock = { navigate: vi.fn().mockResolvedValue(true) };
    confirmarMock = vi.fn().mockResolvedValue(true);
    notificacaoMock = { sucesso: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [ContaListaPageComponent],
      providers: [
        { provide: ContaService, useValue: contaServiceMock },
        { provide: MatDialog, useValue: dialogMock },
        { provide: Router, useValue: routerMock },
        {
          provide: ActivatedRoute,
          useValue: {
            queryParamMap: parametrosUrl.asObservable(),
            snapshot: { queryParams: { page: '1', sort: 'nomeUsuario,desc' } },
          },
        },
        { provide: DialogoConfirmacaoService, useValue: { confirmar: confirmarMock } },
        { provide: NotificacaoService, useValue: notificacaoMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ContaListaPageComponent);
    fixture.detectChanges();
  });

  it('carrega a página conforme filtros e ordenação da URL', () => {
    parametrosUrl.next(convertToParamMap({
      page: '2',
      sort: 'nomeUsuario,desc',
      nomePessoa: 'Ana',
      apenasInativos: 'true',
      perfis: ['GERENCIADOR_PESSOAS', 'GERENCIADOR_ACESSO'],
    }));

    expect(contaServiceMock['listar']).toHaveBeenLastCalledWith({
      page: 2,
      size: 10,
      sort: 'nomeUsuario,desc',
      nomePessoa: 'Ana',
      apenasInativos: true,
      nomeUsuario: undefined,
      email: undefined,
      perfis: ['GERENCIADOR_PESSOAS', 'GERENCIADOR_ACESSO'],
      redefinirSenha: undefined,
      dataCriacao: undefined,
      dataInativo: undefined,
    });
  });

  it('abre filtros e sincroniza valores sanitizados na URL', () => {
    const filtros = {
      nomePessoa: 'Ana Silva',
      perfis: ['GERENCIADOR_PESSOAS'],
      redefinirSenha: true,
      dataCriacao: '14-05-2026',
    };
    dialogMock.open.mockReturnValue({ afterClosed: () => of(filtros) });

    fixture.componentInstance['abrirFiltros']();

    expect(dialogMock.open).toHaveBeenCalledWith(
      ContaFiltroDialogComponent,
      expect.objectContaining({ data: expect.any(Object) }),
    );
    expect(routerMock.navigate).toHaveBeenCalledWith([], expect.objectContaining({
      queryParams: expect.objectContaining({
        page: 0,
        nomePessoa: 'Ana Silva',
        perfis: ['GERENCIADOR_PESSOAS'],
        redefinirSenha: 'true',
        dataCriacao: '14-05-2026',
      }),
      queryParamsHandling: 'merge',
    }));
  });

  it('alterna entre contas ativas e inativas pela URL', () => {
    fixture.componentInstance['alternarInativos']();
    expect(routerMock.navigate).toHaveBeenLastCalledWith([], expect.objectContaining({
      queryParams: { page: 0, apenasInativos: 'true' },
    }));

    routerMock.navigate.mockClear();
    parametrosUrl.next(convertToParamMap({ apenasInativos: 'true' }));
    fixture.componentInstance['alternarInativos']();
    expect(routerMock.navigate).toHaveBeenLastCalledWith([], expect.objectContaining({
      queryParams: { page: 0, apenasInativos: null },
    }));
  });

  it('limpa filtros preservando o estado padrão de contas ativas', () => {
    fixture.componentInstance['limparFiltros']();

    expect(routerMock.navigate).toHaveBeenCalledWith([], expect.objectContaining({
      queryParams: expect.objectContaining({
        page: 0,
        nomePessoa: null,
        perfis: null,
        dataInativo: null,
        apenasInativos: null,
      }),
      queryParamsHandling: 'merge',
    }));
  });

  it('sincroniza paginação e ordenação na URL', () => {
    const component = fixture.componentInstance;
    component['mudarPagina']({
      pageIndex: 3,
      pageSize: 10,
      length: 50,
      previousPageIndex: 0,
    });
    expect(routerMock.navigate).toHaveBeenLastCalledWith([], expect.objectContaining({
      queryParams: { page: 3 },
      queryParamsHandling: 'merge',
    }));

    component['mudarOrdem']({ active: 'nomeUsuario', direction: 'desc' });
    expect(routerMock.navigate).toHaveBeenLastCalledWith([], expect.objectContaining({
      queryParams: { page: 0, sort: 'nomeUsuario,desc' },
      queryParamsHandling: 'merge',
    }));
  });

  it('desativa após confirmação e não chama serviço se cancelar', async () => {
    await fixture.componentInstance['alternarEstado']({ id: 4, ativo: false, nomePessoa: 'Ana Silva' });
    expect(contaServiceMock['desativar']).toHaveBeenCalledWith(4);
    expect(notificacaoMock.sucesso).toHaveBeenCalledWith('Conta desativada com sucesso.');

    confirmarMock.mockResolvedValue(false);
    contaServiceMock['desativar'].mockClear();
    await fixture.componentInstance['alternarEstado']({ id: 4, ativo: false, nomePessoa: 'Ana Silva' });
    expect(contaServiceMock['desativar']).not.toHaveBeenCalled();
  });

  it('reativa conta inativa e abre modal de redefinição com os dados da conta', async () => {
    await fixture.componentInstance['alternarEstado']({ id: 4, ativo: true, nomePessoa: 'Ana Silva' });
    expect(contaServiceMock['reativar']).toHaveBeenCalledWith(4);

    dialogMock.open.mockReturnValue({ afterClosed: () => of(false) });
    fixture.componentInstance['redefinirSenha'](CONTA);
    expect(dialogMock.open).toHaveBeenCalledWith(
      ContaRedefinirSenhaDialogComponent,
      expect.objectContaining({
        data: { contaId: 4, nomePessoa: 'Ana Silva', nomeUsuario: 'ana.silva' },
      }),
    );
  });
});