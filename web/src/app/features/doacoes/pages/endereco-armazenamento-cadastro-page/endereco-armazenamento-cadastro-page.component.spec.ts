import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { DialogoConfirmacaoService } from '@features/shared/services/dialogo-confirmacao.service';
import { NotificacaoService } from '@features/shared/services/notificacao.service';
import { of } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { EnderecoArmazenamentoDetalhe } from '../../models/endereco-armazenamento.model';
import { EnderecoArmazenamentoService } from '../../services/endereco-armazenamento.service';
import { EnderecoArmazenamentoCadastroPageComponent } from './endereco-armazenamento-cadastro-page.component';

const ENDERECO_COM_ITENS: EnderecoArmazenamentoDetalhe = {
  id: 10,
  nome: 'Galpão Principal',
  cep: '77000-000',
  uf: 'TO',
  cidade: 'Palmas',
  bairro: 'Centro',
  logradouro: 'Av JK',
  numero: '100',
  ativo: true,
  itens: [
    {
      idItem: 1,
      nomeItem: 'Arroz 5kg',
      categoria: 'Alimentos',
      unidadeMedida: 'Pacote',
      quantidade: 50,
    },
  ],
};

const ENDERECO_SEM_ITENS: EnderecoArmazenamentoDetalhe = {
  ...ENDERECO_COM_ITENS,
  id: 20,
  itens: [],
};

describe('EnderecoArmazenamentoCadastroPageComponent (US-91)', () => {
  let fixture: ComponentFixture<EnderecoArmazenamentoCadastroPageComponent>;
  let component: EnderecoArmazenamentoCadastroPageComponent;
  let serviceMock: { buscarPorId: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    serviceMock = {
      buscarPorId: vi.fn(),
    };

    await TestBed.configureTestingModule({
      imports: [EnderecoArmazenamentoCadastroPageComponent],
      providers: [
        provideRouter([]),
        { provide: EnderecoArmazenamentoService, useValue: serviceMock },
        {
          provide: NotificacaoService,
          useValue: { sucesso: vi.fn(), erro: vi.fn(), alerta: vi.fn() },
        },
        { provide: DialogoConfirmacaoService, useValue: { confirmar: vi.fn() } },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: { get: () => '10' },
              queryParams: {},
            },
          },
        },
      ],
    }).compileComponents();
  });

  it('exibe tabela com os itens em estoque no modo visualizacao quando houver itens', async () => {
    serviceMock.buscarPorId.mockReturnValue(of(ENDERECO_COM_ITENS));

    fixture = TestBed.createComponent(EnderecoArmazenamentoCadastroPageComponent);
    fixture.componentRef.setInput('id', '10');
    fixture.componentRef.setInput('visualizacao', true);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const element: HTMLElement = fixture.nativeElement;
    const tituloSecao = element.querySelector('h2');
    expect(tituloSecao?.textContent).toContain('Itens em Estoque');

    const celulas = element.querySelectorAll('tbody tr td');
    expect(celulas.length).toBeGreaterThan(0);
    expect(element.textContent).toContain('Arroz 5kg');
    expect(element.textContent).toContain('Alimentos');
    expect(element.textContent).toContain('Pacote');
    expect(element.textContent).toContain('50');
  });

  it('exibe mensagem amigavel quando nao houver itens armazenados no local', async () => {
    serviceMock.buscarPorId.mockReturnValue(of(ENDERECO_SEM_ITENS));

    fixture = TestBed.createComponent(EnderecoArmazenamentoCadastroPageComponent);
    fixture.componentRef.setInput('id', '20');
    fixture.componentRef.setInput('visualizacao', true);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const element: HTMLElement = fixture.nativeElement;
    expect(element.textContent).toContain('Nenhum item armazenado neste local');
  });

  it('nao exibe a tabela de itens quando nao estiver em modo visualizacao', async () => {
    serviceMock.buscarPorId.mockReturnValue(of(ENDERECO_COM_ITENS));

    fixture = TestBed.createComponent(EnderecoArmazenamentoCadastroPageComponent);
    fixture.componentRef.setInput('id', '10');
    fixture.componentRef.setInput('visualizacao', false);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    const element: HTMLElement = fixture.nativeElement;
    expect(element.querySelector('table')).toBeNull();
    expect(element.textContent).not.toContain('Nenhum item armazenado neste local');
  });
});
