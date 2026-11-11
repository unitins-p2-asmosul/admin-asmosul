import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it } from 'vitest';
import { MovimentacaoResumo } from '../../models/movimentacao.model';
import { MovimentacaoTabelaComponent } from './movimentacao-tabela.component';

const MOVIMENTACOES_EXEMPLO: MovimentacaoResumo[] = [
  {
    id: 1,
    nomeItem: 'Arroz 5kg',
    localAntigo: 'Depósito 1',
    localNovo: 'Depósito 2',
    quantidade: '10',
    dataHora: '2026-11-04 16:30:00',
  },
  {
    id: 2,
    nomeItem: 'Feijão',
    localAntigo: 'Depósito 1',
    localNovo: 'Depósito 2',
    quantidade: '5',
    dataHora: '04-11-2026 14:15:00',
  },
];

describe('MovimentacaoTabelaComponent', () => {
  let fixture: ComponentFixture<MovimentacaoTabelaComponent>;
  let component: MovimentacaoTabelaComponent;

  beforeEach(async () => {
    TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [MovimentacaoTabelaComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(MovimentacaoTabelaComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('dados', MOVIMENTACOES_EXEMPLO);
    fixture.componentRef.setInput('totalElementos', 2);
    fixture.componentRef.setInput('tamanhoPagina', 10);
    fixture.componentRef.setInput('paginaAtual', 0);
    fixture.componentRef.setInput('carregando', false);
    fixture.detectChanges();
  });

  it('formata a coluna dataHora para dd/MM/yyyy HH:mm', () => {
    const texto = fixture.nativeElement.textContent;
    expect(texto).toContain('04/11/2026 16:30');
    expect(texto).toContain('04/11/2026 14:15');
    expect(texto).not.toContain('2026-11-04 16:30:00');
    expect(texto).not.toContain('04-11-2026 14:15:00');
  });

  it('exporta a coluna dataHora com formato dd/MM/yyyy HH:mm', () => {
    const colunaDataHora = component.colunasExportaveis.find((c) => c.id === 'dataHora');
    expect(colunaDataHora).toBeDefined();
    expect(colunaDataHora?.valor(MOVIMENTACOES_EXEMPLO[0])).toBe('04/11/2026 16:30');
    expect(colunaDataHora?.valor(MOVIMENTACOES_EXEMPLO[1])).toBe('04/11/2026 14:15');
  });
});
