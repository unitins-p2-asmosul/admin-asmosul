import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { of } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { KitDisponibilidade, KitResumo } from '../../models/kit.model';
import { UnidadeMedidaCodigo } from '../../models/unidade-medida.model';
import { EnderecoArmazenamentoService } from '../../services/endereco-armazenamento.service';
import { KitService } from '../../services/kit.service';
import { KitSelecaoDialogComponent } from './kit-selecao-dialog.component';

const KITS_MOCK: KitResumo[] = [
  {
    id: 1,
    nome: 'Cesta Básica',
    totalItens: 2,
    itensNomes: ['Arroz', 'Feijão'],
    quantidadeDistribuivel: 5,
    ativo: true,
  },
];

const DISPONIBILIDADE_MOCK: KitDisponibilidade = {
  idKit: 1,
  nomeKit: 'Cesta Básica',
  localArmazenamentoId: 100,
  nomeLocalArmazenamento: 'Galpão Central',
  quantidadeDisponivel: 3,
  itens: [
    {
      idItem: 10,
      nomeItem: 'Arroz 5kg',
      unidadeMedida: UnidadeMedidaCodigo.PACOTE,
      quantidadePorKit: 2,
      saldoEstoque: 10,
      kitsPossiveisItem: 5,
    },
    {
      idItem: 20,
      nomeItem: 'Feijão 1kg',
      unidadeMedida: UnidadeMedidaCodigo.PACOTE,
      quantidadePorKit: 3,
      saldoEstoque: 9,
      kitsPossiveisItem: 3,
    },
  ],
};

describe('KitSelecaoDialogComponent', () => {
  let fixture: ComponentFixture<KitSelecaoDialogComponent>;
  let kitServiceMock: {
    listarTodos: ReturnType<typeof vi.fn>;
    consultarDisponibilidade: ReturnType<typeof vi.fn>;
  };
  let enderecoServiceMock: { listarTodas: ReturnType<typeof vi.fn> };
  let dialogRefMock: { close: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    kitServiceMock = {
      listarTodos: vi.fn().mockReturnValue(of(KITS_MOCK)),
      consultarDisponibilidade: vi.fn().mockReturnValue(of(DISPONIBILIDADE_MOCK)),
    };
    enderecoServiceMock = {
      listarTodas: vi.fn().mockReturnValue(of([{ id: 100, nome: 'Galpão Central', ativo: true }])),
    };
    dialogRefMock = {
      close: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [KitSelecaoDialogComponent],
      providers: [
        { provide: KitService, useValue: kitServiceMock },
        { provide: EnderecoArmazenamentoService, useValue: enderecoServiceMock },
        { provide: MatDialogRef, useValue: dialogRefMock },
        {
          provide: MAT_DIALOG_DATA,
          useValue: {
            itensJaIncluidos: [
              // Já tem 3 pacotes de Feijão na lista do mesmo local -> saldo restante de Feijão vira 9 - 3 = 6 -> 6 / 3 = 2 kits possíveis
              {
                idItem: 20,
                nomeItem: 'Feijão 1kg',
                sigla: 'PCT',
                quantidade: 3,
                idArmazenamento: 100,
                nomeArmazenamento: 'Galpão Central',
              },
            ],
          },
        },
      ],
    }).compileComponents();
  });

  it('calcula maxKitsPossiveis descontando itens já incluídos na lista de saída e desmembra itens ao confirmar', async () => {
    fixture = TestBed.createComponent(KitSelecaoDialogComponent);
    fixture.detectChanges();

    const component = fixture.componentInstance;
    component['form'].patchValue({
      idKit: 1,
      idArmazenamento: 100,
      quantidadeKits: 2,
    });
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(kitServiceMock.consultarDisponibilidade).toHaveBeenCalledWith(1, 100);
    expect(component['maxKitsPossiveis']()).toBe(2);

    component['confirmar']();

    expect(dialogRefMock.close).toHaveBeenCalledWith([
      {
        idItem: 10,
        nomeItem: 'Arroz 5kg',
        sigla: 'pct',
        quantidade: 4,
        idArmazenamento: 100,
        nomeArmazenamento: 'Galpão Central',
      },
      {
        idItem: 20,
        nomeItem: 'Feijão 1kg',
        sigla: 'pct',
        quantidade: 6,
        idArmazenamento: 100,
        nomeArmazenamento: 'Galpão Central',
      },
    ]);
  });
});
