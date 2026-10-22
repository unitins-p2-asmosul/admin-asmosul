import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, convertToParamMap, Router } from '@angular/router';
import { DialogoConfirmacaoService } from '@features/shared/services/dialogo-confirmacao.service';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { BehaviorSubject, of } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ItemFiltroDialogComponent } from '../../components/item-filtro-dialog/item-filtro-dialog.component';
import { ItemConsultaParametros, ItemFiltros } from '../../models/item.model';
import { ItemService } from '../../services/item.service';
import { ItemListaPageComponent } from './item-lista-page.component';

describe('ItemListaPageComponent', () => {
  let fixture: ComponentFixture<ItemListaPageComponent>;
  let itemServiceMock: Record<string, ReturnType<typeof vi.fn>>;
  let dialogMock: { open: ReturnType<typeof vi.fn> };
  let routerMock: { navigate: ReturnType<typeof vi.fn> };
  let confirmarMock: ReturnType<typeof vi.fn>;
  let notificacaoMock: { sucesso: ReturnType<typeof vi.fn>; erro: ReturnType<typeof vi.fn> };
  let parametrosUrl: BehaviorSubject<ReturnType<typeof convertToParamMap>>;

  beforeEach(async () => {
    parametrosUrl = new BehaviorSubject(convertToParamMap({}));
    itemServiceMock = {
      listar: vi.fn().mockReturnValue(
        of({
          dados: [],
          paginaAtual: 0,
          tamanhoPagina: 10,
          totalElementos: 0,
          totalPaginas: 0,
        }),
      ),
      desativar: vi.fn().mockReturnValue(of(void 0)),
      reativar: vi.fn().mockReturnValue(of(void 0)),
    };
    dialogMock = {
      open: vi.fn().mockReturnValue({ afterClosed: () => of(undefined) }),
    };
    routerMock = { navigate: vi.fn().mockResolvedValue(true) };
    confirmarMock = vi.fn().mockResolvedValue(true);
    notificacaoMock = { sucesso: vi.fn(), erro: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [ItemListaPageComponent],
      providers: [
        { provide: ItemService, useValue: itemServiceMock },
        { provide: MatDialog, useValue: dialogMock },
        { provide: Router, useValue: routerMock },
        {
          provide: ActivatedRoute,
          useValue: {
            queryParamMap: parametrosUrl.asObservable(),
            snapshot: { queryParams: { page: '0', sort: 'nome,asc' } },
          },
        },
        { provide: DialogoConfirmacaoService, useValue: { confirmar: confirmarMock } },
        { provide: NotificacaoService, useValue: notificacaoMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ItemListaPageComponent);
    fixture.detectChanges();
  });

  it('carrega a página conforme filtros e ordenação da URL', () => {
    parametrosUrl.next(
      convertToParamMap({
        page: '2',
        sort: 'nome,desc',
        nome: 'Feijão',
        apenasInativos: 'true',
        categoriasIds: '1,2',
        estoque: '50',
        precoUnitarioMinimo: '1000',
        precoUnitarioMaximo: '5000',
      }),
    );

    expect(itemServiceMock['listar']).toHaveBeenLastCalledWith<[ItemConsultaParametros]>({
      page: 2,
      size: 10,
      sort: 'nome,desc',
      nome: 'Feijão',
      apenasInativos: true,
      categoriasIds: [1, 2],
      estoque: 50,
      precoUnitarioMinimo: 1000,
      precoUnitarioMaximo: 5000,
    });
  });

  it('abre filtros e sincroniza valores sanitizados na URL', () => {
    const filtros: ItemFiltros = {
      nome: 'Arroz',
      categoriasIds: [3, 4],
      estoque: 100,
      precoUnitarioMinimo: 500,
      precoUnitarioMaximo: 2500,
    };
    dialogMock.open.mockReturnValue({ afterClosed: () => of(filtros) });

    fixture.componentInstance['abrirFiltros']();

    expect(dialogMock.open).toHaveBeenCalledWith(
      ItemFiltroDialogComponent,
      expect.objectContaining({ data: expect.any(Object) }),
    );
    expect(routerMock.navigate).toHaveBeenCalledWith(
      [],
      expect.objectContaining({
        queryParams: expect.objectContaining({
          page: 0,
          nome: 'Arroz',
          categoriasIds: '3,4',
          estoque: 100,
          precoUnitarioMinimo: 500,
          precoUnitarioMaximo: 2500,
        }),
        queryParamsHandling: 'merge',
      }),
    );
  });

  it('alterna entre itens ativos e inativos pela URL', () => {
    fixture.componentInstance['alternarInativos']();
    expect(routerMock.navigate).toHaveBeenLastCalledWith(
      [],
      expect.objectContaining({
        queryParams: { page: 0, apenasInativos: 'true' },
      }),
    );

    routerMock.navigate.mockClear();
    parametrosUrl.next(convertToParamMap({ apenasInativos: 'true' }));
    fixture.componentInstance['alternarInativos']();
    expect(routerMock.navigate).toHaveBeenLastCalledWith(
      [],
      expect.objectContaining({
        queryParams: { page: 0, apenasInativos: null },
      }),
    );
  });

  it('limpa filtros preservando o estado padrão', () => {
    fixture.componentInstance['limparFiltros']();

    expect(routerMock.navigate).toHaveBeenCalledWith(
      [],
      expect.objectContaining({
        queryParams: expect.objectContaining({
          page: 0,
          nome: null,
          categoriasIds: null,
          estoque: null,
          precoUnitarioMinimo: null,
          precoUnitarioMaximo: null,
          apenasInativos: null,
        }),
        queryParamsHandling: 'merge',
      }),
    );
  });

  it('sincroniza paginação e ordenação na URL', () => {
    const component = fixture.componentInstance;
    component['mudarPagina']({
      pageIndex: 3,
      pageSize: 10,
      length: 50,
      previousPageIndex: 0,
    });
    expect(routerMock.navigate).toHaveBeenLastCalledWith(
      [],
      expect.objectContaining({
        queryParams: { page: 3 },
        queryParamsHandling: 'merge',
      }),
    );

    component['mudarOrdem']({ active: 'estoque', direction: 'desc' });
    expect(routerMock.navigate).toHaveBeenLastCalledWith(
      [],
      expect.objectContaining({
        queryParams: { page: 0, sort: 'estoque,desc' },
        queryParamsHandling: 'merge',
      }),
    );
  });

  it('navega para adicionar item', () => {
    fixture.componentInstance['irParaAdicionar']();
    expect(routerMock.navigate).toHaveBeenCalledWith(
      ['adicionar'],
      expect.objectContaining({ relativeTo: expect.anything() }),
    );
  });

  it('navega para visualizar item', () => {
    fixture.componentInstance['visualizar'](15);
    expect(routerMock.navigate).toHaveBeenCalledWith(
      [15],
      expect.objectContaining({ relativeTo: expect.anything() }),
    );
  });

  it('navega para editar item', () => {
    fixture.componentInstance['editar'](15);
    expect(routerMock.navigate).toHaveBeenCalledWith(
      [15, 'editar'],
      expect.objectContaining({ relativeTo: expect.anything() }),
    );
  });

  it('desativa item após confirmação e não chama serviço se cancelar', async () => {
    await fixture.componentInstance['alternarEstado']({ id: 10, ativo: false });
    expect(itemServiceMock['desativar']).toHaveBeenCalledWith(10);
    expect(notificacaoMock.sucesso).toHaveBeenCalledWith('Item inativado com sucesso.');

    confirmarMock.mockResolvedValue(false);
    itemServiceMock['desativar'].mockClear();
    await fixture.componentInstance['alternarEstado']({ id: 10, ativo: false });
    expect(itemServiceMock['desativar']).not.toHaveBeenCalled();
  });

  it('reativa item após confirmação', async () => {
    await fixture.componentInstance['alternarEstado']({ id: 10, ativo: true });
    expect(itemServiceMock['reativar']).toHaveBeenCalledWith(10);
    expect(notificacaoMock.sucesso).toHaveBeenCalledWith('Item reativado com sucesso.');
  });
});
