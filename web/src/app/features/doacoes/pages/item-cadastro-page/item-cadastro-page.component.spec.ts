import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { DialogoConfirmacaoService } from '@features/shared/services/dialogo-confirmacao.service';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { of } from 'rxjs';
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

describe('ItemCadastroPageComponent (US-63)', () => {
  let fixture: ComponentFixture<ItemCadastroPageComponent>;
  let component: ItemCadastroPageComponent;
  let itemServiceMock: { buscarPorId: ReturnType<typeof vi.fn> };
  let categoriaItemServiceMock: { listar: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    itemServiceMock = {
      buscarPorId: vi.fn(),
    };
    categoriaItemServiceMock = {
      listar: vi
        .fn()
        .mockReturnValue(of({ dados: [], totalElementos: 0, paginaAtual: 0, tamanhoPagina: 10 })),
    };

    await TestBed.configureTestingModule({
      imports: [ItemCadastroPageComponent],
      providers: [
        provideRouter([]),
        { provide: ItemService, useValue: itemServiceMock },
        { provide: CategoriaItemService, useValue: categoriaItemServiceMock },
        {
          provide: NotificacaoService,
          useValue: { sucesso: vi.fn(), erro: vi.fn(), alerta: vi.fn() },
        },
        { provide: DialogoConfirmacaoService, useValue: { confirmar: vi.fn() } },
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
    expect(tituloSecao?.textContent).toContain('Distribuição por Local de Armazenamento');

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
