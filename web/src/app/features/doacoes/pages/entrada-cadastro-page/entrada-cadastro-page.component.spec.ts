import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { DialogoConfirmacaoService } from '@features/shared/services/dialogo-confirmacao.service';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { PessoaService } from '@features/pessoas/services/pessoa.service';
import { of } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { EnderecoArmazenamentoService } from '../../services/endereco-armazenamento.service';
import { EntradaService } from '../../services/entrada.service';
import { ItemService } from '../../services/item.service';
import { EntradaCadastroPageComponent } from './entrada-cadastro-page.component';

describe('EntradaCadastroPageComponent', () => {
  let fixture: ComponentFixture<EntradaCadastroPageComponent>;
  let component: EntradaCadastroPageComponent;
  let entradaServiceMock: {
    cadastrar: ReturnType<typeof vi.fn>;
    buscarPorId: ReturnType<typeof vi.fn>;
  };
  let itemServiceMock: { listarTodos: ReturnType<typeof vi.fn> };
  let enderecoServiceMock: { listarTodas: ReturnType<typeof vi.fn> };
  let pessoaServiceMock: { listar: ReturnType<typeof vi.fn> };
  let notificacaoMock: {
    sucesso: ReturnType<typeof vi.fn>;
    alerta: ReturnType<typeof vi.fn>;
    erro: ReturnType<typeof vi.fn>;
  };

  const ITENS_MOCK = [
    { id: 1, nome: 'Arroz 5kg', categoria: 'Alimentos', unidadeMedida: 'UNIDADE', estoque: 10 },
    { id: 2, nome: 'Feijão 1kg', categoria: 'Alimentos', unidadeMedida: 'KG', estoque: 20 },
  ];

  const ENDERECOS_MOCK = [{ id: 10, nome: 'Depósito Central' }];

  const DOADORES_MOCK = [{ id: 100, nome: 'Carlos Doador', cpfCnpj: '12345678900' }];

  beforeEach(async () => {
    TestBed.resetTestingModule();

    entradaServiceMock = {
      cadastrar: vi.fn().mockReturnValue(of({ id: 1 })),
      buscarPorId: vi.fn(),
    };
    itemServiceMock = {
      listarTodos: vi.fn().mockReturnValue(of(ITENS_MOCK)),
    };
    enderecoServiceMock = {
      listarTodas: vi.fn().mockReturnValue(of(ENDERECOS_MOCK)),
    };
    pessoaServiceMock = {
      listar: vi.fn().mockReturnValue(of({ dados: DOADORES_MOCK })),
    };
    notificacaoMock = {
      sucesso: vi.fn(),
      alerta: vi.fn(),
      erro: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [EntradaCadastroPageComponent],
      providers: [
        provideRouter([]),
        { provide: EntradaService, useValue: entradaServiceMock },
        { provide: ItemService, useValue: itemServiceMock },
        { provide: EnderecoArmazenamentoService, useValue: enderecoServiceMock },
        { provide: PessoaService, useValue: pessoaServiceMock },
        { provide: NotificacaoService, useValue: notificacaoMock },
        {
          provide: DialogoConfirmacaoService,
          useValue: { confirmar: vi.fn().mockResolvedValue(true) },
        },
        { provide: ActivatedRoute, useValue: { snapshot: { queryParams: {} } } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(EntradaCadastroPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('deve inicializar e mapear opções de itens e doadores para o select pesquisável', () => {
    expect(component).toBeTruthy();
    expect(component['opcoesItens']().length).toBe(2);
    expect(component['opcoesItens']()[0]).toEqual({
      valor: 1,
      rotulo: 'Arroz 5kg',
      subtexto: 'Categoria: Alimentos',
    });

    expect(component['opcoesDoadores']().length).toBe(1);
    expect(component['opcoesDoadores']()[0].valor).toBe(100);
    expect(component['opcoesDoadores']()[0].rotulo).toBe('Carlos Doador');
    expect(component['opcoesDoadores']()[0].subtexto).toContain('123.456.789-00');
  });

  it('deve incluir item e limpar itemForm mantendo seleção disponível', () => {
    component['itemForm'].patchValue({
      idItem: 1,
      quantidade: 5,
      idArmazenamento: 10,
    });

    component['incluirItem']();
    fixture.detectChanges();

    expect(component['itensIncluidos']().length).toBe(1);
    expect(component['itensIncluidos']()[0].nomeItem).toBe('Arroz 5kg');
    expect(component['itensIncluidos']()[0].quantidade).toBe(5);
    expect(component['itemForm'].controls.idItem.value).toBeNull();
  });

  it('deve salvar entrada quando formulário for válido', () => {
    component['itensIncluidos'].set([
      {
        idItem: 1,
        nomeItem: 'Arroz 5kg',
        sigla: 'UN',
        quantidade: 5,
        idArmazenamento: 10,
        nomeArmazenamento: 'Depósito Central',
      },
    ]);

    component['form'].patchValue({
      idDoador: 100,
      descricao: 'Doação teste',
    });

    component['salvar']();
    expect(entradaServiceMock.cadastrar).toHaveBeenCalled();
  });
});
