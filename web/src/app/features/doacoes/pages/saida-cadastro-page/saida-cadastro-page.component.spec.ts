import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
 import { DialogoConfirmacaoService } from '@features/shared/services/dialogo-confirmacao.service';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { PessoaService } from '@features/pessoas/services/pessoa.service';
import { of } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ItemService } from '../../services/item.service';
import { SaidaService } from '../../services/saida.service';
import { SaidaCadastroPageComponent } from './saida-cadastro-page.component';

describe('SaidaCadastroPageComponent', () => {
  let fixture: ComponentFixture<SaidaCadastroPageComponent>;
  let saidaServiceMock: {
    cadastrar: ReturnType<typeof vi.fn>;
    buscarPorId: ReturnType<typeof vi.fn>;
    atualizar: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    saidaServiceMock = {
      cadastrar: vi.fn().mockReturnValue(of({ id: 1, itens: [] })),
      buscarPorId: vi.fn(),
      atualizar: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [SaidaCadastroPageComponent],
      providers: [
        provideRouter([]),
        { provide: SaidaService, useValue: saidaServiceMock },
        { provide: ItemService, useValue: { listarTodos: vi.fn().mockReturnValue(of([])) } },
        {
          provide: PessoaService,
          useValue: {
            listar: vi.fn().mockReturnValue(of({ dados: [] })),
          },
        },
        {
          provide: NotificacaoService,
          useValue: { sucesso: vi.fn(), erro: vi.fn(), alerta: vi.fn() },
        },
        {
          provide: DialogoConfirmacaoService,
          useValue: { confirmar: vi.fn().mockResolvedValue(true) },
        },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: { get: () => null },
              queryParams: {},
            },
          },
        },
      ],
    }).compileComponents();
  });

  it('agrupa e soma quantidades quando itens com mesmo idItem e idArmazenamento são mesclados', () => {
    fixture = TestBed.createComponent(SaidaCadastroPageComponent);
    fixture.detectChanges();

    const component = fixture.componentInstance;

    component['mesclarItensNaLista']([
      {
        idItem: 10,
        nomeItem: 'Arroz 5kg',
        sigla: 'PCT',
        quantidade: 2,
        idArmazenamento: 100,
        nomeArmazenamento: 'Galpão Central',
      },
    ]);

    // Aplicando um kit que traz o mesmo item no mesmo local + outro item
    component['mesclarItensNaLista']([
      {
        idItem: 10,
        nomeItem: 'Arroz 5kg',
        sigla: 'PCT',
        quantidade: 4,
        idArmazenamento: 100,
        nomeArmazenamento: 'Galpão Central',
      },
      {
        idItem: 20,
        nomeItem: 'Feijão 1kg',
        sigla: 'PCT',
        quantidade: 6,
        idArmazenamento: 100,
        nomeArmazenamento: 'Galpão Central',
      },
    ]);

    expect(component['itensIncluidos']()).toEqual([
      {
        idItem: 10,
        nomeItem: 'Arroz 5kg',
        sigla: 'PCT',
        quantidade: 6,
        idArmazenamento: 100,
        nomeArmazenamento: 'Galpão Central',
      },
      {
        idItem: 20,
        nomeItem: 'Feijão 1kg',
        sigla: 'PCT',
        quantidade: 6,
        idArmazenamento: 100,
        nomeArmazenamento: 'Galpão Central',
      },
    ]);
  });
});
