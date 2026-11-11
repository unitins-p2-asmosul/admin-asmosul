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
  let component: SaidaCadastroPageComponent;
  let saidaServiceMock: {
    cadastrar: ReturnType<typeof vi.fn>;
    buscarPorId: ReturnType<typeof vi.fn>;
  };
  let itemServiceMock: {
    listarTodos: ReturnType<typeof vi.fn>;
    buscarPorId: ReturnType<typeof vi.fn>;
  };
  let pessoaServiceMock: { listar: ReturnType<typeof vi.fn> };
  let notificacaoMock: {
    sucesso: ReturnType<typeof vi.fn>;
    alerta: ReturnType<typeof vi.fn>;
    erro: ReturnType<typeof vi.fn>;
  };

  const ITENS_MOCK = [
    { id: 1, nome: 'Cesta Básica', categoria: 'Alimentos', unidadeMedida: 'UNIDADE', estoque: 15 },
  ];

  const RECEBEDORES_MOCK = [{ id: 200, nome: 'João Beneficiário', cpfCnpj: '98765432100' }];

  beforeEach(async () => {
    TestBed.resetTestingModule();

    saidaServiceMock = {
      cadastrar: vi.fn().mockReturnValue(of({ id: 1 })),
      buscarPorId: vi.fn(),
    };
    itemServiceMock = {
      listarTodos: vi.fn().mockReturnValue(of(ITENS_MOCK)),
      buscarPorId: vi.fn().mockReturnValue(
        of({
          id: 1,
          nome: 'Cesta Básica',
          locais: [{ idEndereco: 10, nomeEndereco: 'Prateleira A', quantidade: 15 }],
        }),
      ),
    };
    pessoaServiceMock = {
      listar: vi.fn().mockReturnValue(of({ dados: RECEBEDORES_MOCK })),
    };
    notificacaoMock = {
      sucesso: vi.fn(),
      alerta: vi.fn(),
      erro: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [SaidaCadastroPageComponent],
      providers: [
        provideRouter([]),
        { provide: SaidaService, useValue: saidaServiceMock },
        { provide: ItemService, useValue: itemServiceMock },
        { provide: PessoaService, useValue: pessoaServiceMock },
        { provide: NotificacaoService, useValue: notificacaoMock },
        {
          provide: DialogoConfirmacaoService,
          useValue: { confirmar: vi.fn().mockResolvedValue(true) },
        },
        { provide: ActivatedRoute, useValue: { snapshot: { queryParams: {} } } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(SaidaCadastroPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('deve inicializar e mapear opções de itens com saldo e recebedores para o select pesquisável', () => {
    expect(component).toBeTruthy();
    expect(component['opcoesItens']().length).toBe(1);
    expect(component['opcoesItens']()[0].valor).toBe(1);
    expect(component['opcoesItens']()[0].rotulo).toBe('Cesta Básica');
    expect(component['opcoesItens']()[0].subtexto).toContain('Saldo Total: 15');

    expect(component['opcoesRecebedores']().length).toBe(1);
    expect(component['opcoesRecebedores']()[0].valor).toBe(200);
    expect(component['opcoesRecebedores']()[0].rotulo).toBe('João Beneficiário');
    expect(component['opcoesRecebedores']()[0].subtexto).toContain('987.654.321-00');
  });

  it('deve incluir item e salvar saída com sucesso', () => {
    component['itemForm'].patchValue({
      idItem: 1,
      quantidade: 3,
    });
    fixture.detectChanges();

    component['localSelecionado'].set(10);
    fixture.detectChanges();

    component['incluirItem']();
    fixture.detectChanges();

    expect(component['itensIncluidos']().length).toBe(1);
    expect(component['itensIncluidos']()[0].nomeItem).toBe('Cesta Básica');
    expect(component['itensIncluidos']()[0].quantidade).toBe(3);

    component['form'].patchValue({
      idRecebedor: 200,
      descricao: 'Saída teste',
    });

    component['salvar']();
    expect(saidaServiceMock.cadastrar).toHaveBeenCalled();
  });
});
