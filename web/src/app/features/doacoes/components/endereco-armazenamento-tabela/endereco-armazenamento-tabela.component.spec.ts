import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it } from 'vitest';
import { EnderecoArmazenamentoResumo } from '../../models/endereco-armazenamento.model';
import { EnderecoArmazenamentoTabelaComponent } from './endereco-armazenamento-tabela.component';

const ENDERECOS_EXEMPLO: EnderecoArmazenamentoResumo[] = [
  {
    id: 1,
    nome: 'Depósito Central',
    cep: '77000-000',
    uf: 'TO',
    cidade: 'Palmas',
    bairro: 'Centro',
    logradouro: 'Avenida JK',
    numero: '100',
    volumeFisico: 120,
    ativo: true,
  },
];

describe('EnderecoArmazenamentoTabelaComponent', () => {
  let fixture: ComponentFixture<EnderecoArmazenamentoTabelaComponent>;
  let component: EnderecoArmazenamentoTabelaComponent;

  beforeEach(async () => {
    TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [EnderecoArmazenamentoTabelaComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(EnderecoArmazenamentoTabelaComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('dados', ENDERECOS_EXEMPLO);
    fixture.componentRef.setInput('totalElementos', 1);
    fixture.componentRef.setInput('tamanhoPagina', 10);
    fixture.componentRef.setInput('paginaAtual', 0);
    fixture.componentRef.setInput('carregando', false);
    fixture.detectChanges();
  });

  it('inclui a coluna Volume Físico nas colunas exibidas e exportáveis', () => {
    expect(component['colunasExibidas']).toEqual([
      'indice',
      'acoes',
      'nome',
      'bairro',
      'logradouro',
      'volumeFisico',
      'status',
    ]);
    expect(component.colunasExportaveis.map((c) => c.id)).toContain('volumeFisico');

    const texto = fixture.nativeElement.textContent;
    expect(texto).toContain('Volume Físico');
    expect(texto).toContain('120');
  });

  it('aplica as classes de status diretamente na celula td sem chip flutuante', () => {
    const celulaStatus = fixture.nativeElement.querySelector('td.pessoa-table__status-cell');
    expect(celulaStatus).not.toBeNull();
    expect(celulaStatus.classList.contains('pessoa-table__status-button')).toBe(true);
    expect(celulaStatus.classList.contains('pessoa-table__status-button--ativo')).toBe(true);
    expect(celulaStatus.querySelector('span')).toBeNull();
    expect(celulaStatus.textContent.trim()).toBe('Ativo');
  });
});
