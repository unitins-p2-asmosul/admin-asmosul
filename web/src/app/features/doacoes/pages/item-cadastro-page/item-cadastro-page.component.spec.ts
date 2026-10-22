import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { DialogoConfirmacaoService } from '@features/shared/services/dialogo-confirmacao.service';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { of, throwError } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ItemDetalhe } from '../../models/item.model';
import { UnidadeMedidaCodigo } from '../../models/unidade-medida.model';
import { CategoriaItemService } from '../../services/categoria-item.service';
import { ItemService } from '../../services/item.service';
import { ItemCadastroPageComponent } from './item-cadastro-page.component';

const ITEM_COM_LOCAIS: ItemDetalhe = {
  id: 1,
  nome: 'Arroz 5kg',
  categoria: { id: 10, nome: 'Alimentos', ativo: true },
  estoque: 150,
  precoUnitario: 2500,
  unidadeMedida: UnidadeMedidaCodigo.PACOTE,
  descricao: 'Arroz tipo 1',
  ativo: true,
  locais: [
    {
      idEndereco: 5,
      nomeLocalArmazenamento: 'Depósito Central',
      quantidade: 150,
    },
  ],
};

const ITEM_SEM_LOCAIS: ItemDetalhe = {
  ...ITEM_COM_LOCAIS,
  id: 2,
  estoque: 0,
  locais: [],
};

describe('ItemCadastroPageComponent', () => {
  let fixture: ComponentFixture<ItemCadastroPageComponent>;
  let itemServiceMock: {
    buscarPorId: ReturnType<typeof vi.fn>;
    cadastrar: ReturnType<typeof vi.fn>;
    atualizar: ReturnType<typeof vi.fn>;
    desativar: ReturnType<typeof vi.fn>;
    reativar: ReturnType<typeof vi.fn>;
  };
  let categoriaItemServiceMock: { listar: ReturnType<typeof vi.fn> };
  let notificacaoMock: {
    sucesso: ReturnType<typeof vi.fn>;
    erro: ReturnType<typeof vi.fn>;
    alerta: ReturnType<typeof vi.fn>;
  };
  let confirmacaoMock: { confirmar: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    itemServiceMock = {
      buscarPorId: vi.fn(),
      cadastrar: vi.fn(),
      atualizar: vi.fn(),
      desativar: vi.fn(),
      reativar: vi.fn(),
    };
    categoriaItemServiceMock = {
      listar: vi
        .fn()
        .mockReturnValue(of({ dados: [], totalElementos: 0, paginaAtual: 0, tamanhoPagina: 10 })),
    };
    notificacaoMock = {
      sucesso: vi.fn(),
      erro: vi.fn(),
      alerta: vi.fn(),
    };
    confirmacaoMock = {
      confirmar: vi.fn().mockResolvedValue(true),
    };

    await TestBed.configureTestingModule({
      imports: [ItemCadastroPageComponent],
      providers: [
        provideRouter([]),
        { provide: ItemService, useValue: itemServiceMock },
        { provide: CategoriaItemService, useValue: categoriaItemServiceMock },
        { provide: NotificacaoService, useValue: notificacaoMock },
        { provide: DialogoConfirmacaoService, useValue: confirmacaoMock },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: { get: () => '1' },
              queryParams: {},
            },
          },
        },
      ],
    }).compileComponents();
  });

  describe('Modo Visualização (US-63)', () => {
    it('exibe tabela de distribuicao por local no modo visualizacao quando houver estoque', async () => {
      itemServiceMock.buscarPorId.mockReturnValue(of(ITEM_COM_LOCAIS));

      fixture = TestBed.createComponent(ItemCadastroPageComponent);
      fixture.componentRef.setInput('id', '1');
      fixture.componentRef.setInput('visualizacao', true);
      fixture.detectChanges();
      await fixture.whenStable();
      fixture.detectChanges();

      const element: HTMLElement = fixture.nativeElement;
      const tituloSecao = element.querySelector('h2');
      expect(tituloSecao?.textContent).toContain('Distribuição do Estoque');

      const celulas = element.querySelectorAll('tbody tr td');
      expect(celulas.length).toBeGreaterThan(0);
      expect(element.textContent).toContain('Depósito Central');
      expect(element.textContent).toContain('150');
    });

    it('exibe mensagem amigavel quando nao houver estoque registrado para o item', async () => {
      itemServiceMock.buscarPorId.mockReturnValue(of(ITEM_SEM_LOCAIS));

      fixture = TestBed.createComponent(ItemCadastroPageComponent);
      fixture.componentRef.setInput('id', '2');
      fixture.componentRef.setInput('visualizacao', true);
      fixture.detectChanges();
      await fixture.whenStable();
      fixture.detectChanges();

      const element: HTMLElement = fixture.nativeElement;
      expect(element.textContent).toContain('Nenhum estoque registrado para este item');
    });

    it('nao exibe a tabela de distribuicao quando nao estiver em modo visualizacao', async () => {
      itemServiceMock.buscarPorId.mockReturnValue(of(ITEM_COM_LOCAIS));

      fixture = TestBed.createComponent(ItemCadastroPageComponent);
      fixture.componentRef.setInput('id', '1');
      fixture.componentRef.setInput('visualizacao', false);
      fixture.detectChanges();
      await fixture.whenStable();
      fixture.detectChanges();

      const element: HTMLElement = fixture.nativeElement;
      expect(element.querySelector('table')).toBeNull();
      expect(element.textContent).not.toContain('Nenhum estoque registrado para este item');
    });
  });

  describe('Cadastro e Edição (US-60 e US-64)', () => {
    it('cadastra novo item convertendo preco em centavos e tratando campos', () => {
      itemServiceMock.cadastrar.mockReturnValue(of(ITEM_COM_LOCAIS));

      fixture = TestBed.createComponent(ItemCadastroPageComponent);
      fixture.detectChanges();

      const component = fixture.componentInstance;
      component['form'].patchValue({
        nome: 'Feijão Carioca',
        idCategoria: 10,
        precoUnitario: 8.5,
        unidadeMedida: UnidadeMedidaCodigo.QUILO,
        descricao: 'Pacote 1kg',
      });

      component['salvar']();

      expect(itemServiceMock.cadastrar).toHaveBeenCalledWith({
        nome: 'Feijão Carioca',
        idCategoria: 10,
        precoUnitario: 850,
        unidadeMedida: UnidadeMedidaCodigo.QUILO,
        descricao: 'Pacote 1kg',
      });
      expect(notificacaoMock.sucesso).toHaveBeenCalledWith('Item cadastrado com sucesso.');
    });

    it('atualiza item existente no modo edicao', async () => {
      itemServiceMock.buscarPorId.mockReturnValue(of(ITEM_COM_LOCAIS));
      itemServiceMock.atualizar.mockReturnValue(of(ITEM_COM_LOCAIS));

      fixture = TestBed.createComponent(ItemCadastroPageComponent);
      fixture.componentRef.setInput('id', '1');
      fixture.detectChanges();
      await fixture.whenStable();
      fixture.detectChanges();

      const component = fixture.componentInstance;
      component['form'].patchValue({
        nome: 'Arroz Integral 5kg',
        precoUnitario: 30.0,
      });

      component['salvar']();

      expect(itemServiceMock.atualizar).toHaveBeenCalledWith(
        1,
        expect.objectContaining({
          nome: 'Arroz Integral 5kg',
          precoUnitario: 3000,
        }),
      );
      expect(notificacaoMock.sucesso).toHaveBeenCalledWith('Item atualizado com sucesso.');
    });

    it('exibe alerta de conflito ao receber status 409', () => {
      const erro409 = new HttpErrorResponse({
        status: 409,
        error: { detail: 'Já existe um item cadastrado com este nome.' },
      });
      itemServiceMock.cadastrar.mockReturnValue(throwError(() => erro409));

      fixture = TestBed.createComponent(ItemCadastroPageComponent);
      fixture.detectChanges();

      const component = fixture.componentInstance;
      component['form'].patchValue({
        nome: 'Arroz 5kg',
        idCategoria: 10,
        precoUnitario: 25.0,
        unidadeMedida: UnidadeMedidaCodigo.PACOTE,
      });

      component['salvar']();

      expect(notificacaoMock.alerta).toHaveBeenCalledWith(
        'Já existe um item cadastrado com este nome.',
      );
      expect(component['form'].controls.nome.errors).toEqual({
        backend: 'Já existe um item cadastrado com este nome.',
      });
    });

    it('inativa item com sucesso a partir de alternarEstado', async () => {
      itemServiceMock.buscarPorId.mockReturnValue(of(ITEM_COM_LOCAIS));
      itemServiceMock.desativar.mockReturnValue(of(void 0));

      fixture = TestBed.createComponent(ItemCadastroPageComponent);
      fixture.componentRef.setInput('id', '1');
      fixture.componentRef.setInput('visualizacao', true);
      fixture.detectChanges();
      await fixture.whenStable();

      const component = fixture.componentInstance;
      await component['alternarEstado']();

      expect(confirmacaoMock.confirmar).toHaveBeenCalledWith(
        'Inativar item',
        'Tem certeza que deseja inativar este item?',
      );
      expect(itemServiceMock.desativar).toHaveBeenCalledWith(1);
      expect(notificacaoMock.sucesso).toHaveBeenCalledWith('Item inativado com sucesso.');
    });
  });
});
