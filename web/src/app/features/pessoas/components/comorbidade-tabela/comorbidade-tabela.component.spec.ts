import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it } from 'vitest';
import { ComorbidadeResumo } from '../../models/comorbidade.model';
import { ComorbidadeTabelaComponent } from './comorbidade-tabela.component';

const COMORBIDADES_EXEMPLO: ComorbidadeResumo[] = [
  {
    id: 1,
    nome: 'Diabetes',
    descricao: 'Tipo 2',
    pessoasAfetadas: 9,
    ativo: true,
  },
];

describe('ComorbidadeTabelaComponent', () => {
  let fixture: ComponentFixture<ComorbidadeTabelaComponent>;
  let component: ComorbidadeTabelaComponent;

  beforeEach(async () => {
    TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [ComorbidadeTabelaComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(ComorbidadeTabelaComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('dados', COMORBIDADES_EXEMPLO);
    fixture.componentRef.setInput('totalElementos', 1);
    fixture.componentRef.setInput('tamanhoPagina', 20);
    fixture.componentRef.setInput('paginaAtual', 0);
    fixture.componentRef.setInput('carregando', false);
    fixture.detectChanges();
  });

  it('inclui a coluna Pessoas Afetadas nas colunas exibidas e exportáveis', () => {
    expect(component['colunasExibidas']).toEqual([
      'id',
      'acoes',
      'nome',
      'descricao',
      'pessoasAfetadas',
      'status',
    ]);
    expect(component.colunasExportaveis.map((c) => c.id)).toContain('pessoasAfetadas');

    const texto = fixture.nativeElement.textContent;
    expect(texto).toContain('Pessoas Afetadas');
    expect(texto).toContain('9');
  });
});
