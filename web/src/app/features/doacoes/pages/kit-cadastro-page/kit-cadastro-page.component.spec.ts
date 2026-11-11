import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { DialogoConfirmacaoService } from '@features/shared/services/dialogo-confirmacao.service';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { of, throwError } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ItemResumo } from '../../models/item.model';
import { KitDetalhe } from '../../models/kit.model';
import { UnidadeMedidaCodigo } from '../../models/unidade-medida.model';
import { ItemService } from '../../services/item.service';
import { KitService } from '../../services/kit.service';
import { KitCadastroPageComponent } from './kit-cadastro-page.component';

const ITENS_CATALOGO: ItemResumo[] = [
  {
    id: 10,
    nome: 'Arroz 5kg',
    categoria: { id: 1, nome: 'Alimentos', ativo: true },
    estoque: 50,
    precoUnitario: 2500,
    unidadeMedida: UnidadeMedidaCodigo.PACOTE,
    ativo: true,
  },
  {
    id: 20,
    nome: 'Feijão 1kg',
    categoria: { id: 1, nome: 'Alimentos', ativo: true },
    estoque: 30,
    precoUnitario: 800,
    unidadeMedida: UnidadeMedidaCodigo.PACOTE,
    ativo: true,
  },
];

const KIT_DETALHE: KitDetalhe = {
  id: 1,
  nome: 'Cesta Básica Familiar',
  descricao: 'Cesta padrão',
  totalItens: 2,
  quantidadeDistribuivel: 10,
  ativo: true,
  itens: [
    {
      idItem: 10,
      nomeItem: 'Arroz 5kg',
      unidadeMedida: UnidadeMedidaCodigo.PACOTE,
      quantidade: 2,
      sobraEstoque: 30,
    },
    {
      idItem: 20,
      nomeItem: 'Feijão 1kg',
      unidadeMedida: UnidadeMedidaCodigo.PACOTE,
      quantidade: 3,
      sobraEstoque: 0,
    },
  ],
};

describe('KitCadastroPageComponent', () => {
  let fixture: ComponentFixture<KitCadastroPageComponent>;
  let kitServiceMock: {
    buscarPorId: ReturnType<typeof vi.fn>;
    cadastrar: ReturnType<typeof vi.fn>;
    atualizar: ReturnType<typeof vi.fn>;
    desativar: ReturnType<typeof vi.fn>;
    reativar: ReturnType<typeof vi.fn>;
  };
  let itemServiceMock: { listarTodos: ReturnType<typeof vi.fn> };
  let notificacaoMock: {
    sucesso: ReturnType<typeof vi.fn>;
    erro: ReturnType<typeof vi.fn>;
    alerta: ReturnType<typeof vi.fn>;
  };
  let confirmacaoMock: { confirmar: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    kitServiceMock = {
      buscarPorId: vi.fn(),
      cadastrar: vi.fn(),
      atualizar: vi.fn(),
      desativar: vi.fn(),
      reativar: vi.fn(),
    };
    itemServiceMock = {
      listarTodos: vi.fn().mockReturnValue(of(ITENS_CATALOGO)),
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
      imports: [KitCadastroPageComponent],
      providers: [
        provideRouter([]),
        { provide: KitService, useValue: kitServiceMock },
        { provide: ItemService, useValue: itemServiceMock },
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

  it('cadastra novo kit normalizando o nome e enviando os itens adicionados', () => {
    kitServiceMock.cadastrar.mockReturnValue(of(KIT_DETALHE));

    fixture = TestBed.createComponent(KitCadastroPageComponent);
    fixture.detectChanges();

    const component = fixture.componentInstance;
    component['form'].patchValue({
      nome: '  Cesta   Básica   Familiar ',
      descricao: 'Cesta padrão',
    });

    component['itemForm'].patchValue({ idItem: 10, quantidade: 2 });
    component['incluirItem']();

    component['salvar']();

    expect(kitServiceMock.cadastrar).toHaveBeenCalledWith({
      nome: 'Cesta Básica Familiar',
      descricao: 'Cesta padrão',
      itens: [{ idItem: 10, quantidade: 2 }],
    });
    expect(notificacaoMock.sucesso).toHaveBeenCalledWith('Kit cadastrado com sucesso.');
  });

  it('impede adicionar o mesmo item duas vezes no mesmo kit', () => {
    fixture = TestBed.createComponent(KitCadastroPageComponent);
    fixture.detectChanges();

    const component = fixture.componentInstance;
    component['itemForm'].patchValue({ idItem: 10, quantidade: 2 });
    component['incluirItem']();

    component['itemForm'].patchValue({ idItem: 10, quantidade: 5 });
    component['incluirItem']();

    expect(component['itensNoKit']().length).toBe(1);
    expect(notificacaoMock.alerta).toHaveBeenCalledWith(
      'Não é permitido adicionar o mesmo item mais de uma vez no kit.',
    );
  });

  it('exibe alerta de conflito ao receber status 409', () => {
    const erro409 = new HttpErrorResponse({
      status: 409,
      error: { detail: 'Já existe um kit cadastrado com este nome.' },
    });
    kitServiceMock.cadastrar.mockReturnValue(throwError(() => erro409));

    fixture = TestBed.createComponent(KitCadastroPageComponent);
    fixture.detectChanges();

    const component = fixture.componentInstance;
    component['form'].patchValue({ nome: 'Cesta Básica' });
    component['itemForm'].patchValue({ idItem: 10, quantidade: 1 });
    component['incluirItem']();
    component['salvar']();

    expect(notificacaoMock.alerta).toHaveBeenCalledWith(
      'Já existe um kit cadastrado com este nome.',
    );
    expect(component['form'].controls.nome.errors).toEqual({
      backend: 'Já existe um kit cadastrado com este nome.',
    });
  });

  it('exibe itens e sobra de estoque no modo visualização', async () => {
    kitServiceMock.buscarPorId.mockReturnValue(of(KIT_DETALHE));

    fixture = TestBed.createComponent(KitCadastroPageComponent);
    fixture.componentRef.setInput('id', '1');
    fixture.componentRef.setInput('visualizacao', true);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const element: HTMLElement = fixture.nativeElement;
    expect(element.textContent).toContain('Arroz 5kg');
    expect(element.textContent).toContain('Feijão 1kg');
    expect(element.textContent).toContain('30 pct');
  });
});
